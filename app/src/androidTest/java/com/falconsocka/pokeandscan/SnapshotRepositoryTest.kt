package com.falconsocka.pokeandscan

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SnapshotRepositoryTest {
    private lateinit var context: Context
    private lateinit var database: SnapshotDatabase
    private lateinit var repository: SnapshotRepository
    private lateinit var testRoot: File
    private lateinit var filesDirectory: File
    private lateinit var databaseName: String

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        databaseName = "snapshot-test-${UUID.randomUUID()}.db"
        testRoot = File(context.cacheDir, "snapshot-test-${UUID.randomUUID()}")
        filesDirectory = File(testRoot, "app-files").apply { mkdirs() }
        database = Room.inMemoryDatabaseBuilder(context, SnapshotDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = SnapshotRepository(database, SnapshotEvidenceStore(filesDirectory))
    }

    @After
    fun tearDown() {
        database.close()
        testRoot.deleteRecursively()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun snapshotsAndNamesSurviveDatabaseReopen() = runBlocking {
        val persistentDatabase = Room.databaseBuilder(context, SnapshotDatabase::class.java, databaseName)
            .allowMainThreadQueries()
            .build()
        val originalRepository = SnapshotRepository(persistentDatabase, SnapshotEvidenceStore(filesDirectory))
        val createdAt = LocalDate.of(2026, 9, 25).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val snapshotId = originalRepository.createSnapshot("Community Day", AppLanguage.English, SnapshotSourceType.MP4, createdAt)
        val secondId = originalRepository.createSnapshot(null, AppLanguage.Slovak, null, createdAt)
        persistentDatabase.snapshotDao().insertRecord(
            PokemonRecordEntity(
                id = UUID.randomUUID().toString(),
                snapshotId = snapshotId,
                sequenceIndex = 0,
                speciesId = "pikachu",
                formId = null,
                cp = 1200,
                ivAttack = 10,
                ivDefense = 11,
                ivStamina = 12,
                ivPercent = 73.3,
                status = SnapshotRecordStatus.READY
            )
        )
        assertTrue(originalRepository.renameSnapshot(snapshotId, "October catches"))
        persistentDatabase.close()

        val reopened = Room.databaseBuilder(context, SnapshotDatabase::class.java, databaseName)
            .allowMainThreadQueries()
            .build()
        try {
            val stored = SnapshotRepository(reopened, SnapshotEvidenceStore(filesDirectory))
                .observeSnapshots()
                .first()

            assertEquals(setOf(secondId, snapshotId), stored.map(SnapshotSummary::id).toSet())
            val renamed = stored.single { it.id == snapshotId }
            val generated = stored.single { it.id == secondId }
            assertEquals("October catches", renamed.name)
            assertEquals("Sken 25.09.2026", generated.name)
            assertEquals(SnapshotSourceType.MP4, renamed.sourceType)
            assertEquals(SnapshotLifecycle.PROCESSING, renamed.lifecycle)
            assertEquals(null, renamed.scopeCompleteness)
            assertEquals(1, renamed.recordCount)
            assertEquals(1, renamed.includedRecordCount)
        } finally {
            reopened.close()
        }
    }

    @Test
    fun deletionCascadesOnlySelectedSnapshotAndItsEvidence() = runBlocking {
        val deletedId = repository.createSnapshot("To remove", AppLanguage.English, SnapshotSourceType.MP4)
        val retainedId = repository.createSnapshot("Keep", AppLanguage.English, SnapshotSourceType.LIVE)
        val recordId = UUID.randomUUID().toString()
        val evidencePath = repositoryEvidenceFile(deletedId, "species.webp")
        val externalVideo = File(testRoot, "outside/original-screen-recording.mp4").apply {
            parentFile!!.mkdirs()
            writeBytes(byteArrayOf(1, 2, 3, 4))
        }

        database.snapshotDao().insertRecord(
            PokemonRecordEntity(
                id = recordId,
                snapshotId = deletedId,
                sequenceIndex = 0,
                speciesId = "pikachu",
                formId = null,
                cp = 1200,
                ivAttack = 10,
                ivDefense = 11,
                ivStamina = 12,
                ivPercent = 73.3,
                status = SnapshotRecordStatus.NEEDS_REVIEW
            )
        )
        database.snapshotDao().insertReviewIssue(
            ReviewIssueEntity(UUID.randomUUID().toString(), deletedId, recordId, "species", "Uncertain text", false, 10L)
        )
        database.snapshotDao().insertEvidenceCrop(
            EvidenceCropEntity(UUID.randomUUID().toString(), deletedId, recordId, "species", "snapshots/$deletedId/evidence/species.webp", 20L)
        )
        database.snapshotDao().insertWarning(
            SnapshotWarningEntity(UUID.randomUUID().toString(), deletedId, "MISSED_APPRAISAL", "No stable screen", 30L)
        )

        val summary = repository.observeSnapshots().first().single { it.id == deletedId }
        assertEquals(1, summary.recordCount)
        assertEquals(1, summary.reviewCount)
        assertEquals(1, summary.warningCount)
        assertTrue(evidencePath.isFile)
        assertTrue(externalVideo.isFile)
        assertTrue(repository.deleteSnapshot(deletedId))

        assertFalse(database.snapshotDao().snapshotExists(deletedId))
        assertEquals(0, database.snapshotDao().recordCount(deletedId))
        assertEquals(0, database.snapshotDao().reviewIssueCount(deletedId))
        assertEquals(0, database.snapshotDao().evidenceCropCount(deletedId))
        assertEquals(0, database.snapshotDao().warningCount(deletedId))
        assertTrue(database.snapshotDao().snapshotExists(retainedId))
        assertEquals(0, database.snapshotDao().recordCount(retainedId))
        assertFalse(evidencePath.exists())
        assertTrue(externalVideo.isFile)
    }

    @Test
    fun evidenceStagingFailureLeavesSnapshotAndLocalEvidenceAvailable() = runBlocking {
        val snapshotId = repository.createSnapshot("Keep intact", AppLanguage.English, SnapshotSourceType.LIVE)
        val evidence = repositoryEvidenceFile(snapshotId, "field.webp")
        val pendingDirectory = File(File(filesDirectory, "snapshots"), ".pending-delete").apply { mkdirs() }
        File(pendingDirectory, snapshotId).mkdirs()

        val failed = runCatching { repository.deleteSnapshot(snapshotId) }.isFailure

        assertTrue(failed)
        assertTrue(database.snapshotDao().snapshotExists(snapshotId))
        assertTrue(evidence.isFile)
    }

    private fun repositoryEvidenceFile(snapshotId: String, name: String): File =
        File(SnapshotEvidenceStore(filesDirectory).evidenceDirectory(snapshotId), name).apply {
            parentFile!!.mkdirs()
            writeBytes(byteArrayOf(5, 6, 7))
        }
}
