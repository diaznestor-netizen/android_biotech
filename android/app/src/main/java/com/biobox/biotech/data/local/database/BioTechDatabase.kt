package com.biobox.biotech.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.biobox.biotech.data.local.dao.*
import com.biobox.biotech.data.local.entity.*

@Database(
    entities = [
        MachineEntity::class,
        ProjectEntity::class,
        InspectionEntity::class,
        ActivityEntity::class,
        GoalEntity::class,
        MissionEntity::class,
        IncidentEntity::class,
        DocumentEntity::class,
        CalendarEventEntity::class,
        UserEntity::class,
        SyncOperationEntity::class,
        MaterialEntity::class,
        EvidenceEntity::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BioTechDatabase : RoomDatabase() {
    abstract fun machineDao(): MachineDao
    abstract fun projectDao(): ProjectDao
    abstract fun inspectionDao(): InspectionDao
    abstract fun activityDao(): ActivityDao
    abstract fun goalDao(): GoalDao
    abstract fun missionDao(): MissionDao
    abstract fun incidentDao(): IncidentDao
    abstract fun documentDao(): DocumentDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun userDao(): UserDao
    abstract fun syncOperationDao(): SyncOperationDao
    abstract fun materialDao(): MaterialDao
    abstract fun evidenceDao(): EvidenceDao

    companion object {
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create new table
                db.execSQL("""
                    CREATE TABLE projects_new (
                        localId TEXT NOT NULL PRIMARY KEY,
                        remoteId INTEGER,
                        codigo TEXT NOT NULL,
                        nombre TEXT NOT NULL,
                        descripcion TEXT,
                        cliente TEXT,
                        responsableId INTEGER,
                        responsableNombre TEXT,
                        usuarioCreadorId INTEGER,
                        estado TEXT NOT NULL,
                        prioridad TEXT NOT NULL,
                        fechaInicio INTEGER,
                        fechaFinEstimada INTEGER,
                        fechaFinReal INTEGER,
                        porcentajeAvance INTEGER NOT NULL,
                        observaciones TEXT,
                        version INTEGER NOT NULL DEFAULT 0,
                        syncStatus TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        deletedAt INTEGER,
                        organizationId TEXT,
                        tenantId TEXT
                    )
                """.trimIndent())

                // 2. Copy data
                db.execSQL("""
                    INSERT INTO projects_new (
                        localId, remoteId, codigo, nombre, descripcion, responsableNombre, 
                        estado, prioridad, porcentajeAvance, syncStatus, createdAt, updatedAt
                    )
                    SELECT CAST(id AS TEXT), id, codigo, nombre, descripcion, responsableNombre, 
                           estado, prioridad, porcentajeAvance, 'SYNCED', 1600000000000, 1600000000000 
                    FROM projects
                """.trimIndent())

                // 3. Swap tables
                db.execSQL("DROP TABLE projects")
                db.execSQL("ALTER TABLE projects_new RENAME TO projects")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_projects_codigo_organizationId_tenantId ON projects(codigo, organizationId, tenantId)")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE projects ADD COLUMN conflictPayloadJson TEXT")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_projects_codigo_organizationId_tenantId ON projects(codigo, organizationId, tenantId)")
                db.execSQL("ALTER TABLE sync_operations ADD COLUMN userId TEXT")
                db.execSQL("ALTER TABLE sync_operations ADD COLUMN organizationId TEXT")
                db.execSQL("ALTER TABLE sync_operations ADD COLUMN tenantId TEXT")
                db.execSQL("ALTER TABLE sync_operations ADD COLUMN idempotencyKey TEXT")
                db.execSQL("ALTER TABLE sync_operations ADD COLUMN conflictPayloadJson TEXT")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sync_operations ADD COLUMN parentEntityLocalId TEXT")
                db.execSQL("ALTER TABLE sync_operations ADD COLUMN schemaVersion INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE sync_operations ADD COLUMN lastHttpStatusCode INTEGER")
                db.execSQL("ALTER TABLE sync_operations ADD COLUMN duration INTEGER")
                db.execSQL("ALTER TABLE sync_operations ADD COLUMN traceId TEXT")

                db.execSQL("UPDATE sync_operations SET status = 'IN_PROGRESS' WHERE status = 'SYNCING'")
                db.execSQL("UPDATE sync_operations SET status = 'FAILED_RETRY' WHERE status = 'FAILED'")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE missions ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'SYNCED'")
                db.execSQL("ALTER TABLE goals ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'SYNCED'")
                db.execSQL("ALTER TABLE pending_inspections ADD COLUMN remoteId INTEGER")
                db.execSQL("""
                    CREATE TABLE evidence (
                        id TEXT NOT NULL PRIMARY KEY,
                        ownerType TEXT NOT NULL,
                        ownerLocalId TEXT NOT NULL,
                        localPath TEXT NOT NULL,
                        mimeType TEXT NOT NULL,
                        remoteUrl TEXT,
                        syncStatus TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX index_evidence_ownerType_ownerLocalId ON evidence(ownerType, ownerLocalId)")
                db.execSQL("CREATE INDEX index_evidence_syncStatus ON evidence(syncStatus)")
            }
        }
    }
}
