package com.biobox.biotech.core.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.biobox.biotech.data.remote.dto.UserDto
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionDataStore @Inject constructor(private val context: Context) {
    private val gson = Gson()

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "biotech_secure_session",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val _authToken = MutableStateFlow<String?>(null)
    val authToken: Flow<String?> = _authToken.asStateFlow()

    private val _userData = MutableStateFlow<UserDto?>(null)
    val userData: Flow<UserDto?> = _userData.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: Flow<Boolean> = _isDarkMode.asStateFlow()

    private val _lastReAuthTime = MutableStateFlow(0L)
    val lastReAuthTime: Flow<Long> = _lastReAuthTime.asStateFlow()

    init {
        _authToken.value = prefs.getString(KEY_ACCESS_TOKEN, null)
        _userData.value = prefs.getString(KEY_USER, null)?.let { json ->
            try { gson.fromJson(json, UserDto::class.java) } catch (_: Exception) { null }
        }
        _isDarkMode.value = prefs.getBoolean(KEY_DARK_MODE, false)
        _lastReAuthTime.value = prefs.getLong(KEY_LAST_REAUTH, 0L)
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER = "user"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_LAST_REAUTH = "last_reauth"
    }

    suspend fun saveSession(accessToken: String, user: UserDto) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_USER, gson.toJson(user))
            .putLong(KEY_LAST_REAUTH, System.currentTimeMillis())
            .apply()
        _authToken.value = accessToken
        _userData.value = user
        _lastReAuthTime.value = System.currentTimeMillis()
    }

    suspend fun updateReAuthTime() {
        val now = System.currentTimeMillis()
        prefs.edit().putLong(KEY_LAST_REAUTH, now).apply()
        _lastReAuthTime.value = now
    }

    suspend fun updateAccessToken(accessToken: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, accessToken).apply()
        _authToken.value = accessToken
    }

    suspend fun saveThemePreference(isDarkMode: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, isDarkMode).apply()
        _isDarkMode.value = isDarkMode
    }

    suspend fun clearSession() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_USER)
            .apply()
        _authToken.value = null
        _userData.value = null
    }
}
