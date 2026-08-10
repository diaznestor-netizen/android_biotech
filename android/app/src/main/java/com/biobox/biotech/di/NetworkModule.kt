package com.biobox.biotech.di

import com.biobox.biotech.BuildConfig
import com.biobox.biotech.core.network.AuthInterceptor
import com.biobox.biotech.core.network.TokenAuthenticator
import com.biobox.biotech.core.util.AppConstants
import com.biobox.biotech.data.remote.api.ActivityService
import com.biobox.biotech.data.remote.api.AlertService
import com.biobox.biotech.data.remote.api.AnalyticsService
import com.biobox.biotech.data.remote.api.AuthService
import com.biobox.biotech.data.remote.api.CalendarService
import com.biobox.biotech.data.remote.api.DocumentService
import com.biobox.biotech.data.remote.api.GoalService
import com.biobox.biotech.data.remote.api.IncidentService
import com.biobox.biotech.data.remote.api.InspectionService
import com.biobox.biotech.data.remote.api.MachineService
import com.biobox.biotech.data.remote.api.MaterialService
import com.biobox.biotech.data.remote.api.MissionService
import com.biobox.biotech.data.remote.api.ProjectService
import com.biobox.biotech.data.remote.api.ReportService
import com.biobox.biotech.data.remote.api.SystemService
import com.biobox.biotech.data.remote.api.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        redactHeader("Authorization")
        redactHeader("Cookie")
        redactHeader("Set-Cookie")
    }

    @Provides
    @Singleton
    fun provideCookieManager(): CookieManager = CookieManager().apply {
        setCookiePolicy(CookiePolicy.ACCEPT_ALL)
    }

    @Provides
    @Singleton
    @Named("BasicOkHttp")
    fun provideBasicOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        cookieManager: CookieManager
    ): OkHttpClient = OkHttpClient.Builder()
        .cookieJar(JavaNetCookieJar(cookieManager))
.addInterceptor(loggingInterceptor)
        .connectTimeout(AppConstants.TIMEOUT_CONEXION_SEG, TimeUnit.SECONDS)
        .readTimeout(AppConstants.TIMEOUT_LECTURA_SEG, TimeUnit.SECONDS)
        .writeTimeout(AppConstants.TIMEOUT_CONEXION_SEG, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @Named("AuthenticatedOkHttp")
    fun provideAuthenticatedOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        cookieManager: CookieManager
    ): OkHttpClient = OkHttpClient.Builder()
        .cookieJar(JavaNetCookieJar(cookieManager))
        .addInterceptor(loggingInterceptor)
.addInterceptor(authInterceptor)
        .authenticator(tokenAuthenticator)
        .connectTimeout(AppConstants.TIMEOUT_CONEXION_SEG, TimeUnit.SECONDS)
        .readTimeout(AppConstants.TIMEOUT_LECTURA_SEG, TimeUnit.SECONDS)
        .writeTimeout(AppConstants.TIMEOUT_CONEXION_SEG, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @Named("AuthRetrofit")
    fun provideAuthRetrofit(@Named("BasicOkHttp") okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @Named("MainRetrofit")
    fun provideMainRetrofit(@Named("AuthenticatedOkHttp") okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideAuthService(@Named("AuthRetrofit") retrofit: Retrofit): AuthService = retrofit.create(AuthService::class.java)

    @Provides
    @Singleton
    @Named("AuthenticatedAuthService")
    fun provideAuthenticatedAuthService(@Named("MainRetrofit") retrofit: Retrofit): AuthService = retrofit.create(AuthService::class.java)

    @Provides
    @Singleton
    fun provideSystemService(@Named("AuthRetrofit") retrofit: Retrofit): SystemService = retrofit.create(SystemService::class.java)

    @Provides
    @Singleton
    fun provideMachineService(@Named("MainRetrofit") retrofit: Retrofit): MachineService = retrofit.create(MachineService::class.java)

    @Provides
    @Singleton
    fun provideProjectService(@Named("MainRetrofit") retrofit: Retrofit): ProjectService = retrofit.create(ProjectService::class.java)

    @Provides
    @Singleton
    fun provideMaterialService(@Named("MainRetrofit") retrofit: Retrofit): MaterialService = retrofit.create(MaterialService::class.java)

    @Provides
    @Singleton
    fun provideInspectionService(@Named("MainRetrofit") retrofit: Retrofit): InspectionService = retrofit.create(InspectionService::class.java)

    @Provides
    @Singleton
    fun provideActivityService(@Named("MainRetrofit") retrofit: Retrofit): ActivityService = retrofit.create(ActivityService::class.java)

    @Provides
    @Singleton
    fun provideAlertService(@Named("MainRetrofit") retrofit: Retrofit): AlertService = retrofit.create(AlertService::class.java)

    @Provides
    @Singleton
    fun provideGoalService(@Named("MainRetrofit") retrofit: Retrofit): GoalService = retrofit.create(GoalService::class.java)

    @Provides
    @Singleton
    fun provideMissionService(@Named("MainRetrofit") retrofit: Retrofit): MissionService = retrofit.create(MissionService::class.java)

    @Provides
    @Singleton
    fun provideIncidentService(@Named("MainRetrofit") retrofit: Retrofit): IncidentService = retrofit.create(IncidentService::class.java)

    @Provides
    @Singleton
    fun provideDocumentService(@Named("MainRetrofit") retrofit: Retrofit): DocumentService = retrofit.create(DocumentService::class.java)

    @Provides
    @Singleton
    fun provideAnalyticsService(@Named("MainRetrofit") retrofit: Retrofit): AnalyticsService = retrofit.create(AnalyticsService::class.java)

    @Provides
    @Singleton
    fun provideCalendarService(@Named("MainRetrofit") retrofit: Retrofit): CalendarService = retrofit.create(CalendarService::class.java)

    @Provides
    @Singleton
    fun provideUserService(@Named("MainRetrofit") retrofit: Retrofit): UserService = retrofit.create(UserService::class.java)

    @Provides
    @Singleton
fun provideReportService(@Named("MainRetrofit") retrofit: Retrofit): ReportService = retrofit.create(ReportService::class.java)
}


