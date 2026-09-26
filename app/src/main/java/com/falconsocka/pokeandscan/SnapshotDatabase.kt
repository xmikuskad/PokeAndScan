package com.falconsocka.pokeandscan

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.withTransaction
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

enum class SnapshotLifecycle { SETUP, PROCESSING, INCOMPLETE, COMPLETE }
enum class SnapshotScopeCompleteness { INTENDED_RANGE, PARTIAL }
enum class ScanScopeType { WHOLE_COLLECTION, FILTERED_SUBSET }
enum class SnapshotSourceType { LIVE, MP4 }
enum class SnapshotRecordStatus { READY, NEEDS_REVIEW, PARTIAL, EXCLUDED }

@Entity(tableName = "snapshots")
data class SnapshotEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAtMillis: Long,
    val sourceType: SnapshotSourceType?,
    val lifecycle: SnapshotLifecycle,
    val scopeCompleteness: SnapshotScopeCompleteness?,
    val scanScopeType: ScanScopeType? = null,
    val scanScopeDescription: String? = null
)

@Entity(
    tableName = "pokemon_records",
    foreignKeys = [ForeignKey(
        entity = SnapshotEntity::class,
        parentColumns = ["id"],
        childColumns = ["snapshotId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("snapshotId")]
)
data class PokemonRecordEntity(
    @PrimaryKey val id: String,
    val snapshotId: String,
    val sequenceIndex: Int,
    val speciesId: String?,
    val formId: String?,
    val cp: Int?,
    val ivAttack: Int?,
    val ivDefense: Int?,
    val ivStamina: Int?,
    val ivPercent: Double?,
    val status: SnapshotRecordStatus
)

@Entity(
    tableName = "review_issues",
    foreignKeys = [
        ForeignKey(
            entity = SnapshotEntity::class,
            parentColumns = ["id"],
            childColumns = ["snapshotId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PokemonRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("snapshotId"), Index("recordId")]
)
data class ReviewIssueEntity(
    @PrimaryKey val id: String,
    val snapshotId: String,
    val recordId: String,
    val fieldName: String,
    val reason: String,
    val isResolved: Boolean,
    val createdAtMillis: Long
)

@Entity(
    tableName = "evidence_crops",
    foreignKeys = [
        ForeignKey(
            entity = SnapshotEntity::class,
            parentColumns = ["id"],
            childColumns = ["snapshotId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PokemonRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("snapshotId"), Index("recordId")]
)
data class EvidenceCropEntity(
    @PrimaryKey val id: String,
    val snapshotId: String,
    val recordId: String,
    val fieldName: String,
    val relativePath: String,
    val sourceOffsetMillis: Long?
)

@Entity(
    tableName = "snapshot_warnings",
    foreignKeys = [ForeignKey(
        entity = SnapshotEntity::class,
        parentColumns = ["id"],
        childColumns = ["snapshotId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("snapshotId")]
)
data class SnapshotWarningEntity(
    @PrimaryKey val id: String,
    val snapshotId: String,
    val type: String,
    val description: String,
    val sourceOffsetMillis: Long?
)

data class SnapshotSummary(
    val id: String,
    val name: String,
    val createdAtMillis: Long,
    val sourceType: SnapshotSourceType?,
    val lifecycle: SnapshotLifecycle,
    val scopeCompleteness: SnapshotScopeCompleteness?,
    val scanScopeType: ScanScopeType?,
    val scanScopeDescription: String?,
    val recordCount: Int,
    val includedRecordCount: Int,
    val reviewCount: Int,
    val partialCount: Int,
    val warningCount: Int
)

data class SnapshotDetailSummary(
    val id: String,
    val name: String,
    val createdAtMillis: Long,
    val sourceType: SnapshotSourceType?,
    val lifecycle: SnapshotLifecycle,
    val scopeCompleteness: SnapshotScopeCompleteness?,
    val scanScopeType: ScanScopeType?,
    val scanScopeDescription: String?,
    val recordCount: Int,
    val includedRecordCount: Int,
    val reviewCount: Int,
    val partialCount: Int,
    val warningCount: Int
)

@Dao
interface SnapshotDao {
    @Query(
        """
        SELECT s.id AS id,
               s.name AS name,
               s.createdAtMillis AS createdAtMillis,
               s.sourceType AS sourceType,
               s.lifecycle AS lifecycle,
               s.scopeCompleteness AS scopeCompleteness,
               s.scanScopeType AS scanScopeType,
               s.scanScopeDescription AS scanScopeDescription,
               (SELECT COUNT(*) FROM pokemon_records r WHERE r.snapshotId = s.id) AS recordCount,
               (SELECT COUNT(*) FROM pokemon_records r WHERE r.snapshotId = s.id AND r.status != 'EXCLUDED') AS includedRecordCount,
               (SELECT COUNT(*) FROM review_issues i WHERE i.snapshotId = s.id AND i.isResolved = 0) AS reviewCount,
               (SELECT COUNT(*) FROM pokemon_records r WHERE r.snapshotId = s.id AND r.status = 'PARTIAL') AS partialCount,
               (SELECT COUNT(*) FROM snapshot_warnings w WHERE w.snapshotId = s.id) AS warningCount
        FROM snapshots s
        ORDER BY s.createdAtMillis DESC, s.id DESC
        """
    )
    fun observeSnapshots(): Flow<List<SnapshotSummary>>

    @Query(
        """
        SELECT s.id AS id,
               s.name AS name,
               s.createdAtMillis AS createdAtMillis,
               s.sourceType AS sourceType,
               s.lifecycle AS lifecycle,
               s.scopeCompleteness AS scopeCompleteness,
               s.scanScopeType AS scanScopeType,
               s.scanScopeDescription AS scanScopeDescription,
               (SELECT COUNT(*) FROM pokemon_records r WHERE r.snapshotId = s.id) AS recordCount,
               (SELECT COUNT(*) FROM pokemon_records r WHERE r.snapshotId = s.id AND r.status != 'EXCLUDED') AS includedRecordCount,
               (SELECT COUNT(*) FROM review_issues i WHERE i.snapshotId = s.id AND i.isResolved = 0) AS reviewCount,
               (SELECT COUNT(*) FROM pokemon_records r WHERE r.snapshotId = s.id AND r.status = 'PARTIAL') AS partialCount,
               (SELECT COUNT(*) FROM snapshot_warnings w WHERE w.snapshotId = s.id) AS warningCount
        FROM snapshots s
        WHERE s.id = :snapshotId
        LIMIT 1
        """
    )
    fun observeSnapshot(snapshotId: String): Flow<SnapshotDetailSummary?>

    @Insert
    suspend fun insertSnapshot(snapshot: SnapshotEntity)

    @Insert
    suspend fun insertRecord(record: PokemonRecordEntity)

    @Insert
    suspend fun insertReviewIssue(issue: ReviewIssueEntity)

    @Insert
    suspend fun insertEvidenceCrop(evidence: EvidenceCropEntity)

    @Insert
    suspend fun insertWarning(warning: SnapshotWarningEntity)

    @Query("UPDATE snapshots SET name = :name WHERE id = :snapshotId")
    suspend fun renameSnapshot(snapshotId: String, name: String): Int

    @Query("""
        UPDATE snapshots
        SET name = :name, sourceType = :sourceType, scanScopeType = :scanScopeType,
            scanScopeDescription = :scanScopeDescription
        WHERE id = :snapshotId AND lifecycle = 'SETUP'
    """)
    suspend fun updateSetupSnapshot(
        snapshotId: String,
        name: String,
        sourceType: SnapshotSourceType,
        scanScopeType: ScanScopeType,
        scanScopeDescription: String?
    ): Int

    @Query("SELECT EXISTS(SELECT 1 FROM snapshots WHERE lifecycle IN ('PROCESSING', 'INCOMPLETE'))")
    suspend fun hasActiveOrRecoverableJob(): Boolean

    @Query("UPDATE snapshots SET lifecycle = :lifecycle WHERE id = :snapshotId AND lifecycle = :expectedLifecycle")
    suspend fun updateLifecycle(
        snapshotId: String,
        expectedLifecycle: SnapshotLifecycle,
        lifecycle: SnapshotLifecycle
    ): Int

    @Query("SELECT EXISTS(SELECT 1 FROM snapshots WHERE id = :snapshotId)")
    suspend fun snapshotExists(snapshotId: String): Boolean

    @Query("SELECT name FROM snapshots WHERE id = :snapshotId AND lifecycle = 'SETUP' LIMIT 1")
    suspend fun setupSnapshotName(snapshotId: String): String?

    @Query("SELECT COUNT(*) FROM pokemon_records WHERE snapshotId = :snapshotId")
    suspend fun recordCount(snapshotId: String): Int

    @Query("SELECT COUNT(*) FROM review_issues WHERE snapshotId = :snapshotId")
    suspend fun reviewIssueCount(snapshotId: String): Int

    @Query("SELECT COUNT(*) FROM evidence_crops WHERE snapshotId = :snapshotId")
    suspend fun evidenceCropCount(snapshotId: String): Int

    @Query("SELECT COUNT(*) FROM snapshot_warnings WHERE snapshotId = :snapshotId")
    suspend fun warningCount(snapshotId: String): Int

    @Query("DELETE FROM snapshots WHERE id = :snapshotId")
    suspend fun deleteSnapshot(snapshotId: String): Int
}

class SnapshotConverters {
    @TypeConverter
    fun lifecycleToString(value: SnapshotLifecycle?): String? = value?.name

    @TypeConverter
    fun lifecycleFromString(value: String?): SnapshotLifecycle? = value?.let(SnapshotLifecycle::valueOf)

    @TypeConverter
    fun scopeToString(value: SnapshotScopeCompleteness?): String? = value?.name

    @TypeConverter
    fun scopeFromString(value: String?): SnapshotScopeCompleteness? = value?.let(SnapshotScopeCompleteness::valueOf)

    @TypeConverter
    fun sourceToString(value: SnapshotSourceType?): String? = value?.name

    @TypeConverter
    fun sourceFromString(value: String?): SnapshotSourceType? = value?.let(SnapshotSourceType::valueOf)

    @TypeConverter
    fun scanScopeToString(value: ScanScopeType?): String? = value?.name

    @TypeConverter
    fun scanScopeFromString(value: String?): ScanScopeType? = value?.let(ScanScopeType::valueOf)

    @TypeConverter
    fun recordStatusToString(value: SnapshotRecordStatus?): String? = value?.name

    @TypeConverter
    fun recordStatusFromString(value: String?): SnapshotRecordStatus? = value?.let(SnapshotRecordStatus::valueOf)
}

@Database(
    entities = [
        SnapshotEntity::class,
        PokemonRecordEntity::class,
        ReviewIssueEntity::class,
        EvidenceCropEntity::class,
        SnapshotWarningEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(SnapshotConverters::class)
abstract class SnapshotDatabase : RoomDatabase() {
    abstract fun snapshotDao(): SnapshotDao

    companion object {
        @Volatile
        private var instance: SnapshotDatabase? = null

        fun get(context: Context): SnapshotDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                SnapshotDatabase::class.java,
                "pokeandscan_snapshots.db"
            ).addMigrations(MIGRATION_1_2).build().also { instance = it }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE snapshots ADD COLUMN scanScopeType TEXT")
                db.execSQL("ALTER TABLE snapshots ADD COLUMN scanScopeDescription TEXT")
            }
        }
    }
}
