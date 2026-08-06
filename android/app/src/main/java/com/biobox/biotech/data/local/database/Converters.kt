package com.biobox.biotech.data.local.database

import androidx.room.TypeConverter
import com.biobox.biotech.core.common.SyncStatus

class Converters {
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String {
        return status.name
    }

    @TypeConverter
    fun toSyncStatus(status: String): SyncStatus {
        return SyncStatus.valueOf(status)
    }

    @TypeConverter
    fun fromSyncOpStatus(status: com.biobox.biotech.data.local.entity.SyncOperationStatus): String {
        return status.name
    }

    @TypeConverter
    fun toSyncOpStatus(status: String): com.biobox.biotech.data.local.entity.SyncOperationStatus {
        return com.biobox.biotech.data.local.entity.SyncOperationStatus.valueOf(status)
    }
}
