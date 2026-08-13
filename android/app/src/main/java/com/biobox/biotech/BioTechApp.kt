package com.biobox.biotech

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.StrictMode
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidApp
class BioTechApp : Application(), Configuration.Provider, ImageLoaderFactory {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    // Lazy: el cliente autenticado (y su cadena SessionDataStore/KeyStore) solo se
    // construye cuando Coil carga la primera imagen, no al crear la Application.
    @Inject @Named("AuthenticatedOkHttp") lateinit var imageOkHttpClient: Lazy<OkHttpClient>

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    // Coil usa este ImageLoader en toda la app (AsyncImage incluido), con el
    // AuthInterceptor y TokenAuthenticator ya configurados: las evidencias bajo
    // /api/v1/evidence/{id} requieren el header Authorization.
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this).okHttpClient { imageOkHttpClient.get() }.build()

    override fun onCreate() {
        val startTime = System.currentTimeMillis()
        super.onCreate()
        setupStrictMode()
        createNotificationChannels()
        val duration = System.currentTimeMillis() - startTime
        Log.d("Performance", "App Start Duration: ${duration}ms")
    }

    private fun setupStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .penaltyFlashScreen()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL,
                "BioTech",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de BioTech"
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL = "biotech_general"
    }
}
