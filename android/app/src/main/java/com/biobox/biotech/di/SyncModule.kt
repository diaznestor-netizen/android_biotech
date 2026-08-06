package com.biobox.biotech.di

import com.biobox.biotech.data.repository.ActivitySyncHandler
import com.biobox.biotech.data.repository.GoalSyncHandler
import com.biobox.biotech.data.repository.IncidentSyncHandler
import com.biobox.biotech.data.repository.InspectionSyncHandler
import com.biobox.biotech.data.repository.MachineSyncHandler
import com.biobox.biotech.data.repository.MissionSyncHandler
import com.biobox.biotech.data.repository.ProjectSyncHandler
import com.biobox.biotech.data.sync.GlobalSyncManagerImpl
import com.biobox.biotech.domain.sync.GlobalSyncManager
import com.biobox.biotech.domain.sync.SyncHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.Multibinds
import dagger.multibindings.StringKey
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    @Singleton
    abstract fun bindGlobalSyncManager(impl: GlobalSyncManagerImpl): GlobalSyncManager

    @Multibinds
    abstract fun syncHandlers(): Map<String, SyncHandler>

    @Binds
    @IntoMap
    @StringKey("PROJECT")
    abstract fun bindProjectSyncHandler(impl: ProjectSyncHandler): SyncHandler

    @Binds
    @IntoMap
    @StringKey("MACHINE")
    abstract fun bindMachineSyncHandler(impl: MachineSyncHandler): SyncHandler

    @Binds
    @IntoMap
    @StringKey("INSPECTION")
    abstract fun bindInspectionSyncHandler(impl: InspectionSyncHandler): SyncHandler

    @Binds
    @IntoMap
    @StringKey("INCIDENT")
    abstract fun bindIncidentSyncHandler(impl: IncidentSyncHandler): SyncHandler

    @Binds
    @IntoMap
    @StringKey("ACTIVITY")
    abstract fun bindActivitySyncHandler(impl: ActivitySyncHandler): SyncHandler

    @Binds
    @IntoMap
    @StringKey("GOAL")
    abstract fun bindGoalSyncHandler(impl: GoalSyncHandler): SyncHandler

    @Binds
    @IntoMap
    @StringKey("MISSION")
    abstract fun bindMissionSyncHandler(impl: MissionSyncHandler): SyncHandler
}
