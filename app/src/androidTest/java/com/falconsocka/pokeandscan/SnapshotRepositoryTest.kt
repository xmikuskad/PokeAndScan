package com.falconsocka.pokeandscan

import android.content.Context
import android.content.res.Configuration
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
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
        val fallbackName = defaultSnapshotName(context, AppLanguage.Slovak, createdAt)
        val slovakContext = context.createConfigurationContext(
            Configuration(context.resources.configuration).apply {
                setLocale(Locale.forLanguageTag(AppLanguage.Slovak.languageTag))
            }
        )
        assertTrue(fallbackName.startsWith(slovakContext.getString(R.string.default_scan_name, "").trim()))
        val snapshotId = originalRepository.createSetupSnapshot(
            "Community Day",
            defaultSnapshotName(context, AppLanguage.English, createdAt),
            SnapshotSourceType.MP4,
            ScanScopeType.FILTERED_SUBSET,
            "Community Day",
            createdAt
        )
        val secondId = originalRepository.createSetupSnapshot(
            "",
            fallbackName,
            SnapshotSourceType.LIVE,
            ScanScopeType.WHOLE_COLLECTION,
            null,
            createdAt
        )
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
            assertEquals(fallbackName, generated.name)
            assertEquals(SnapshotSourceType.MP4, renamed.sourceType)
            assertEquals(SnapshotLifecycle.SETUP, renamed.lifecycle)
            assertEquals(null, renamed.scopeCompleteness)
            assertEquals(ScanScopeType.FILTERED_SUBSET, renamed.scanScopeType)
            assertEquals("Community Day", renamed.scanScopeDescription)
            assertEquals(1, renamed.recordCount)
            assertEquals(1, renamed.includedRecordCount)
        } finally {
            reopened.close()
        }
    }

    @Test
    fun deletionCascadesOnlySelectedSnapshotAndItsEvidence() = runBlocking {
        val deletedId = repository.createSetupSnapshot(
            "To remove",
            defaultSnapshotName(context, AppLanguage.English),
            SnapshotSourceType.MP4,
            ScanScopeType.WHOLE_COLLECTION,
            null
        )
        val retainedId = repository.createSetupSnapshot(
            "Keep",
            defaultSnapshotName(context, AppLanguage.English),
            SnapshotSourceType.LIVE,
            ScanScopeType.WHOLE_COLLECTION,
            null
        )
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
        val snapshotId = repository.createSnapshot(
            "Keep intact",
            defaultSnapshotName(context, AppLanguage.English),
            SnapshotSourceType.LIVE
        )
        val evidence = repositoryEvidenceFile(snapshotId, "field.webp")
        val pendingDirectory = File(File(filesDirectory, "snapshots"), ".pending-delete").apply { mkdirs() }
        File(pendingDirectory, snapshotId).mkdirs()

        val failed = runCatching { repository.deleteSnapshot(snapshotId) }.isFailure

        assertTrue(failed)
        assertTrue(database.snapshotDao().snapshotExists(snapshotId))
        assertTrue(evidence.isFile)
    }

    @Test
    fun setupSessionsHaveIndependentIdentityAndCanBeUpdatedWithoutReplacement() = runBlocking {
        val firstId = repository.createSetupSnapshot(
            "First",
            "Scan Sep 25, 2026",
            SnapshotSourceType.LIVE,
            ScanScopeType.WHOLE_COLLECTION,
            null
        )
        val secondId = repository.createSetupSnapshot(
            "Second",
            "Scan Sep 25, 2026",
            SnapshotSourceType.MP4,
            ScanScopeType.FILTERED_SUBSET,
            "Gym defenders"
        )

        assertFalse(firstId == secondId)
        assertTrue(repository.updateSetupSnapshot(
            firstId,
            "First updated",
            SnapshotSourceType.MP4,
            ScanScopeType.FILTERED_SUBSET,
            "Tag typed by user"
        ))

        val stored = repository.observeSnapshots().first()
        assertEquals(2, stored.size)
        assertEquals("First updated", stored.single { it.id == firstId }.name)
        assertEquals(ScanScopeType.FILTERED_SUBSET, stored.single { it.id == firstId }.scanScopeType)
        assertEquals("Tag typed by user", stored.single { it.id == firstId }.scanScopeDescription)
        assertEquals("Second", stored.single { it.id == secondId }.name)
        assertEquals("Gym defenders", stored.single { it.id == secondId }.scanScopeDescription)
    }

    @Test
    fun activeJobPreventsStartingAnotherJobOrCreatingAnotherSetup() = runBlocking {
        val firstSetupId = repository.createSetupSnapshot(
            "Ready to start",
            "Scan Sep 25, 2026",
            SnapshotSourceType.LIVE,
            ScanScopeType.WHOLE_COLLECTION,
            null
        )
        val secondSetupId = repository.createSetupSnapshot(
            "Waiting setup",
            "Scan Sep 25, 2026",
            SnapshotSourceType.MP4,
            ScanScopeType.WHOLE_COLLECTION,
            null
        )

        assertTrue(repository.startProcessing(firstSetupId))
        assertFalse(repository.startProcessing(secondSetupId))
        assertTrue(runCatching {
            repository.createSnapshot("Another job", "Scan Sep 25, 2026", SnapshotSourceType.MP4)
        }.exceptionOrNull() is ActiveScanJobException)
        assertTrue(runCatching {
            repository.createSetupSnapshot(
                "Another setup",
                "Scan Sep 25, 2026",
                SnapshotSourceType.MP4,
                ScanScopeType.WHOLE_COLLECTION,
                null
            )
        }.exceptionOrNull() is ActiveScanJobException)
        assertEquals(setOf(firstSetupId, secondSetupId), repository.observeSnapshots().first().map { it.id }.toSet())
    }

    @Test
    fun blankSetupNameUsesCreationLanguageAndUnsafeNamesAreRejected() = runBlocking {
        val createdAt = LocalDate.of(2026, 9, 25).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val slovakName = defaultSnapshotName(context, AppLanguage.Slovak, createdAt)
        val snapshotId = repository.createSetupSnapshot(
            " \t ",
            slovakName,
            SnapshotSourceType.LIVE,
            ScanScopeType.WHOLE_COLLECTION,
            null,
            createdAt
        )
        val englishName = defaultSnapshotName(context, AppLanguage.English, createdAt)
        val englishId = repository.createSetupSnapshot(
            "",
            englishName,
            SnapshotSourceType.MP4,
            ScanScopeType.WHOLE_COLLECTION,
            null,
            createdAt
        )

        val storedNames = repository.observeSnapshots().first().associate { it.id to it.name }
        assertEquals(slovakName, storedNames[snapshotId])
        assertEquals(englishName, storedNames[englishId])
        assertTrue(runCatching {
            repository.createSetupSnapshot(
                "Bad\u0000Name",
                "Scan Sep 25, 2026",
                SnapshotSourceType.LIVE,
                ScanScopeType.WHOLE_COLLECTION,
                null
            )
        }.isFailure)
        assertTrue(runCatching {
            repository.createSetupSnapshot(
                "x".repeat(MAX_SCAN_NAME_LENGTH + 1),
                "Scan Sep 25, 2026",
                SnapshotSourceType.LIVE,
                ScanScopeType.WHOLE_COLLECTION,
                null
            )
        }.isFailure)
    }

    private fun repositoryEvidenceFile(snapshotId: String, name: String): File =
        File(SnapshotEvidenceStore(filesDirectory).evidenceDirectory(snapshotId), name).apply {
            parentFile!!.mkdirs()
            writeBytes(byteArrayOf(5, 6, 7))
        }
}
