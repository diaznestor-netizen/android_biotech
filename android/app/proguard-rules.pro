# BioTech v1.0 - ProGuard/R8 Rules

# Model Preservation (DTOs & Entities)
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses
-keep class com.biobox.biotech.data.remote.dto.** { *; }
-keep class com.biobox.biotech.data.local.entity.** { *; }
-keep class com.biobox.biotech.domain.model.** { *; }
-keep class com.biobox.biotech.data.repository.SyncConflictPayload { *; }

# Gson preservation
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements com.google.gson.TypeAdapterFactory
-keep public class * implements com.google.gson.JsonSerializer
-keep public class * implements com.google.gson.JsonDeserializer
-keep public class * implements com.google.gson.TypeAdapter

# Retrofit & OkHttp
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn javax.annotation.**
-keepattributes RuntimeVisibleAlphaAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Hilt & Dagger
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponentManager { *; }

# Room
-keep class * extends androidx.room.RoomDatabase { *; }

# WorkManager
-keep class * extends androidx.work.ListenableWorker { *; }

# Compose
-keep class androidx.compose.ui.platform.AbstractComposeView { *; }
-keep class androidx.compose.foundation.lazy.** { *; }

# BuildConfig
-keep class com.biobox.biotech.BuildConfig { *; }
