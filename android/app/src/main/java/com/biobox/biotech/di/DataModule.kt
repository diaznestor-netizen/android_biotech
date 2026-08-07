package com.biobox.biotech.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.biobox.biotech.core.datastore.SessionDataStore
import com.biobox.biotech.data.local.dao.*
import com.biobox.biotech.data.local.database.BioTechDatabase
import com.biobox.biotech.data.repository.*
import com.biobox.biotech.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds @Singleton abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun bindMachineRepository(impl: MachineRepositoryImpl): MachineRepository
    @Binds @Singleton abstract fun bindProjectRepository(impl: ProjectRepositoryImpl): ProjectRepository
    @Binds @Singleton abstract fun bindInspectionRepository(impl: InspectionRepositoryImpl): InspectionRepository
    @Binds @Singleton abstract fun bindActivityRepository(impl: ActivityRepositoryImpl): ActivityRepository
    @Binds @Singleton abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository
    @Binds @Singleton abstract fun bindMissionRepository(impl: MissionRepositoryImpl): MissionRepository
    @Binds @Singleton abstract fun bindIncidentRepository(impl: IncidentRepositoryImpl): IncidentRepository
    @Binds @Singleton abstract fun bindDocumentRepository(impl: DocumentRepositoryImpl): DocumentRepository
    @Binds @Singleton abstract fun bindAnalyticsRepository(impl: AnalyticsRepositoryImpl): AnalyticsRepository
    @Binds @Singleton abstract fun bindCalendarRepository(impl: CalendarRepositoryImpl): CalendarRepository
    @Binds @Singleton abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
    @Binds @Singleton abstract fun bindMaterialRepository(impl: MaterialRepositoryImpl): MaterialRepository

    companion object {
        @Provides @Singleton
        fun provideSessionDataStore(@ApplicationContext context: Context): SessionDataStore = SessionDataStore(context)

        @Provides @Singleton
        fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)

        @Provides @Singleton
        fun provideDatabase(@ApplicationContext context: Context): BioTechDatabase {
            return Room.databaseBuilder(context, BioTechDatabase::class.java, "biotech_db")
                .addMigrations(BioTechDatabase.MIGRATION_5_6)
                .addMigrations(BioTechDatabase.MIGRATION_6_7)
                .addMigrations(BioTechDatabase.MIGRATION_7_8)
                .build()
        }

        @Provides fun provideMachineDao(db: BioTechDatabase): MachineDao = db.machineDao()
        @Provides fun provideProjectDao(db: BioTechDatabase): ProjectDao = db.projectDao()
        @Provides fun provideInspectionDao(db: BioTechDatabase): InspectionDao = db.inspectionDao()
        @Provides fun provideActivityDao(db: BioTechDatabase): ActivityDao = db.activityDao()
        @Provides fun provideGoalDao(db: BioTechDatabase): GoalDao = db.goalDao()
        @Provides fun provideMissionDao(db: BioTechDatabase): MissionDao = db.missionDao()
        @Provides fun provideIncidentDao(db: BioTechDatabase): IncidentDao = db.incidentDao()
        @Provides fun provideDocumentDao(db: BioTechDatabase): DocumentDao = db.documentDao()
        @Provides fun provideCalendarEventDao(db: BioTechDatabase): CalendarEventDao = db.calendarEventDao()
        @Provides fun provideUserDao(db: BioTechDatabase): UserDao = db.userDao()
        @Provides fun provideSyncOperationDao(db: BioTechDatabase): SyncOperationDao = db.syncOperationDao()
        @Provides fun provideMaterialDao(db: BioTechDatabase): MaterialDao = db.materialDao()
    }
}
