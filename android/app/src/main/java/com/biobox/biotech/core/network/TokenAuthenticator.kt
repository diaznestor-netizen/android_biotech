package com.biobox.biotech.core.network

import com.biobox.biotech.core.datastore.SessionDataStore
import com.biobox.biotech.data.remote.api.AuthService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

class TokenAuthenticator @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val authServiceProvider: Provider<AuthService>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) {
            runBlocking { sessionDataStore.clearSession() }
            return null
        }

        synchronized(this) {
            val currentToken = runBlocking { sessionDataStore.authToken.first() }
            if (!currentToken.isNullOrBlank() && response.request.header("Authorization") != "Bearer $currentToken") {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            return runBlocking {
                try {
                    val refreshResponse = authServiceProvider.get().refresh()
                    if (!refreshResponse.isSuccessful) {
                        sessionDataStore.clearSession()
                        return@runBlocking null
                    }

                    val body = refreshResponse.body() ?: return@runBlocking null
                    val accessToken = body.tokens?.accessToken ?: return@runBlocking null
                    val user = body.user ?: return@runBlocking null
                    sessionDataStore.saveSession(
                        accessToken = accessToken,
                        user = user
                    )
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $accessToken")
                        .build()
                } catch (_: Exception) {
                    sessionDataStore.clearSession()
                    null
                }
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            result++
            priorResponse = priorResponse.priorResponse
        }
        return result
    }
}
