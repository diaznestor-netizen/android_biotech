package com.biobox.biotech.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.biobox.biotech.data.local.database.BioTechDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class BioTechDatabaseMigrationTest {

    private val dbName = "migration-test.db"
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(dbName)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(dbName)
    }

    @Test
    fun migrate6To7KeepsProjectData() = runBlocking {
        createVersion6Database(context.getDatabasePath(dbName))

        val db = Room.databaseBuilder(context, BioTechDatabase::class.java, dbName)
            .addMigrations(BioTechDatabase.MIGRATION_6_7)
            .allowMainThreadQueries()
            .build()

        val project = db.projectDao().getProjectByLocalId("local-1")
        val operation = db.syncOperationDao().getOperationsByEntity("local-1", "PROJECT").firstOrNull()

        assertNotNull(project)
        assertEquals("PRJ-MIG", project?.codigo)
        assertEquals("org-a", project?.organizationId)
        assertNotNull(operation)
        assertEquals("op-1", operation?.id)

        db.close()
    }

    private fun createVersion6Database(dbFile: File) {
        dbFile.parentFile?.mkdirs()
        val db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        copyUnchangedVersion6Schema(db)
        db.execSQL(
            """
            CREATE TABLE projects (
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
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE sync_operations (
                id TEXT NOT NULL PRIMARY KEY,
                entityType TEXT NOT NULL,
                entityLocalId TEXT NOT NULL,
                operation TEXT NOT NULL,
                payloadJson TEXT NOT NULL,
                status TEXT NOT NULL,
                retryCount INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                lastError TEXT
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE UNIQUE INDEX index_sync_operations_entityType_entityLocalId_operation
            ON sync_operations(entityType, entityLocalId, operation)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO projects (
                localId, remoteId, codigo, nombre, descripcion, cliente, responsableId,
                responsableNombre, usuarioCreadorId, estado, prioridad, fechaInicio,
                fechaFinEstimada, fechaFinReal, porcentajeAvance, observaciones, version,
                syncStatus, createdAt, updatedAt, deletedAt, organizationId, tenantId
            ) VALUES (
                'local-1', 7, 'PRJ-MIG', 'Migrado', NULL, NULL, NULL, NULL, 1,
                'PLANEADO', 'MEDIA', NULL, NULL, NULL, 10, NULL, 1, 'SYNCED',
                1, 1, NULL, 'org-a', 'tenant-a'
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO sync_operations (
                id, entityType, entityLocalId, operation, payloadJson, status, retryCount, createdAt, updatedAt, lastError
            ) VALUES (
                'op-1', 'PROJECT', 'local-1', 'CREATE', '{}', 'PENDING', 0, 1, 1, NULL
            )
            """.trimIndent()
        )
        db.version = 6
        db.close()
    }

    private fun copyUnchangedVersion6Schema(targetDb: SQLiteDatabase) {
        val templateName = "migration-template.db"
        context.deleteDatabase(templateName)
        val templateDb = Room.databaseBuilder(context, BioTechDatabase::class.java, templateName)
            .allowMainThreadQueries()
            .build()
        // Force Room to create the on-disk database before we reopen it with SQLiteDatabase.
        templateDb.openHelper.writableDatabase.close()
        templateDb.close()

        val templateFile = context.getDatabasePath(templateName)
        val templateSqlite = SQLiteDatabase.openDatabase(templateFile.path, null, SQLiteDatabase.OPEN_READONLY)
        templateSqlite.rawQuery(
            """
            SELECT type, name, sql
            FROM sqlite_master
            WHERE sql IS NOT NULL
              AND name NOT IN ('android_metadata', 'sqlite_sequence', 'room_master_table', 'projects', 'sync_operations')
            ORDER BY CASE type WHEN 'table' THEN 0 ELSE 1 END, name
            """.trimIndent(),
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val sql = cursor.getString(2)
                val normalizedSql = sql?.lowercase()
                val referencesManuallyManagedTables =
                    normalizedSql?.contains("`projects`") == true ||
                        normalizedSql?.contains(" sync_operations ") == true ||
                        normalizedSql?.contains("`sync_operations`") == true
                if (!sql.isNullOrBlank() && !referencesManuallyManagedTables) {
                    targetDb.execSQL(sql)
                }
            }
        }
        templateSqlite.close()
        context.deleteDatabase(templateName)
    }
}
