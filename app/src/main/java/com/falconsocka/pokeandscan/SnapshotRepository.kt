package com.falconsocka.pokeandscan

import android.content.Context
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class SnapshotRepository(
    private val database: SnapshotDatabase,
    private val evidenceStore: SnapshotEvidenceStore
) {
    private val dao get() = database.snapshotDao()

    fun observeSnapshots(): Flow<List<SnapshotSummary>> = dao.observeSnapshots()

    fun observeSnapshot(snapshotId: String): Flow<SnapshotDetailSummary?> = dao.observeSnapshot(snapshotId)

    suspend fun createSnapshot(
        displayName: String?,
        language: AppLanguage,
        sourceType: SnapshotSourceType?,
        createdAtMillis: Long = System.currentTimeMillis()
    ): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val name = displayName?.trim()?.takeIf(String::isNotEmpty)
            ?: generatedSnapshotName(language, createdAtMillis)
        database.withTransaction {
            dao.insertSnapshot(
                SnapshotEntity(
                    id = id,
                    name = name,
                    createdAtMillis = createdAtMillis,
                    sourceType = sourceType,
                    lifecycle = SnapshotLifecycle.PROCESSING,
                    scopeCompleteness = null
                )
            )
        }
        id
    }

    suspend fun renameSnapshot(snapshotId: String, displayName: String): Boolean = withContext(Dispatchers.IO) {
        val name = displayName.trim()
        require(name.isNotEmpty()) { "Snapshot name cannot be empty." }
        dao.renameSnapshot(snapshotId, name) == 1
    }

    suspend fun deleteSnapshot(snapshotId: String): Boolean = withContext(Dispatchers.IO) {
        val ticket = evidenceStore.stageDeletion(snapshotId)
        val snapshotWasDeleted = try {
            database.withTransaction {
                if (dao.snapshotExists(snapshotId)) {
                    dao.deleteSnapshot(snapshotId)
                    true
                } else {
                    false
                }
            }
            // The staged file will be removed after the database cascade commits.
        } catch (error: Throwable) {
            ticket?.restore()
            throw error
        }
        try {
            ticket?.purge()
        } catch (_: IOException) {
            // The staged directory is retried during the next startup recovery.
        }
        snapshotWasDeleted
    }

    suspend fun recoverPendingEvidenceDeletions() = withContext(Dispatchers.IO) {
        for (snapshotId in evidenceStore.pendingDeletionIds()) {
            if (dao.snapshotExists(snapshotId)) {
                evidenceStore.restorePendingDeletion(snapshotId)
            } else {
                evidenceStore.purgePendingDeletion(snapshotId)
            }
        }
    }

    companion object {
        fun from(context: Context): SnapshotRepository = SnapshotRepository(
            database = SnapshotDatabase.get(context),
            evidenceStore = SnapshotEvidenceStore(context.filesDir)
        )

        fun generatedSnapshotName(language: AppLanguage, createdAtMillis: Long): String {
            val date = Instant.ofEpochMilli(createdAtMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.forLanguageTag(language.languageTag)))
            val prefix = if (language == AppLanguage.Slovak) "Sken" else "Scan"
            return "$prefix $date"
        }
    }
}

/** Manages evidence crops under app-private storage, never the imported source URI. */
class SnapshotEvidenceStore(private val filesDirectory: File) {
    private val snapshotsDirectory = File(filesDirectory, "snapshots")
    private val pendingDirectory = File(snapshotsDirectory, ".pending-delete")

    fun evidenceDirectory(snapshotId: String): File = File(snapshotDirectory(snapshotId), "evidence")

    internal fun stageDeletion(snapshotId: String): EvidenceDeletionTicket? {
        validateSnapshotId(snapshotId)
        val original = evidenceDirectory(snapshotId)
        val staged = File(pendingDirectory, snapshotId)
        if (staged.exists()) {
            if (original.exists()) throw IOException("Both active and staged evidence exist for snapshot $snapshotId.")
            return EvidenceDeletionTicket(original, staged)
        }
        if (!original.exists()) return null
        if (!pendingDirectory.exists() && !pendingDirectory.mkdirs()) {
            throw IOException("Could not prepare the snapshot evidence deletion.")
        }
        if (!original.renameTo(staged)) {
            throw IOException("Could not safely stage snapshot evidence for deletion.")
        }
        return EvidenceDeletionTicket(original, staged)
    }

    internal fun pendingDeletionIds(): List<String> {
        if (!pendingDirectory.isDirectory) return emptyList()
        return pendingDirectory.listFiles()
            .orEmpty()
            .filter(File::isDirectory)
            .map(File::getName)
            .filter { runCatching { validateSnapshotId(it) }.isSuccess }
    }

    internal fun restorePendingDeletion(snapshotId: String) {
        validateSnapshotId(snapshotId)
        EvidenceDeletionTicket(evidenceDirectory(snapshotId), File(pendingDirectory, snapshotId)).restore()
    }

    internal fun purgePendingDeletion(snapshotId: String) {
        validateSnapshotId(snapshotId)
        EvidenceDeletionTicket(evidenceDirectory(snapshotId), File(pendingDirectory, snapshotId)).purge()
    }

    private fun snapshotDirectory(snapshotId: String): File {
        validateSnapshotId(snapshotId)
        return File(snapshotsDirectory, snapshotId)
    }

    private fun validateSnapshotId(snapshotId: String) {
        try {
            UUID.fromString(snapshotId)
        } catch (_: IllegalArgumentException) {
            throw IllegalArgumentException("Snapshot IDs must be UUIDs.")
        }
    }
}

internal class EvidenceDeletionTicket(
    private val originalDirectory: File,
    private val stagedDirectory: File
) {
    fun restore() {
        if (!stagedDirectory.exists()) return
        if (!originalDirectory.parentFile!!.exists() && !originalDirectory.parentFile!!.mkdirs()) {
            throw IOException("Could not restore snapshot evidence.")
        }
        if (!stagedDirectory.renameTo(originalDirectory)) {
            throw IOException("Could not restore snapshot evidence.")
        }
    }

    fun purge() {
        if (!stagedDirectory.exists()) return
        stagedDirectory.walkBottomUp().forEach { file ->
            if (!file.delete()) throw IOException("Could not remove app-owned snapshot evidence.")
        }
        stagedDirectory.parentFile?.let { parent ->
            if (parent.isDirectory && parent.list().isNullOrEmpty()) parent.delete()
        }
        originalDirectory.parentFile?.let { parent ->
            if (parent.isDirectory && parent.list().isNullOrEmpty()) parent.delete()
        }
    }
}
