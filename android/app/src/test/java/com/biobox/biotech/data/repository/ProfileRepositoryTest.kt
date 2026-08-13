package com.biobox.biotech.data.repository

import com.biobox.biotech.core.datastore.SessionDataStore
import com.biobox.biotech.data.remote.api.ProfileService
import com.biobox.biotech.data.remote.dto.UserDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class ProfileRepositoryTest {
    private val service = mockk<ProfileService>()
    private val sessionDataStore = mockk<SessionDataStore>(relaxed = true)
    private val repository = ProfileRepositoryImpl(service, sessionDataStore)
    private val dto = UserDto(7, "Ana", "Torres", "ana@example.com", "5512345678", "SUPERVISOR")

    @Test
    fun updateProfilePersistsUpdatedUserInSession() = runTest {
        val updated = dto.copy(nombre = "Anita")
        coEvery { service.updateProfile(any()) } returns Response.success(updated)

        val result = repository.updateProfile("Anita", "Torres", "ana@example.com")

        assertTrue(result.isSuccess)
        assertEquals("Anita", result.getOrNull()?.nombre)
        coVerify { sessionDataStore.updateUser(updated) }
    }

    @Test
    fun updateProfileOmitsBlankEmailInRequest() = runTest {
        coEvery { service.updateProfile(any()) } returns Response.success(dto)

        repository.updateProfile("Ana", "Torres", "   ")

        coVerify { service.updateProfile(match { it.email == null && it.nombre == "Ana" && it.apellido == "Torres" }) }
    }

    @Test
    fun updateProfileMapsServerErrorMessage() = runTest {
        coEvery { service.updateProfile(any()) } returns Response.error<UserDto>(
            400,
            """{"error":"La contraseña debe tener al menos 8 caracteres"}""".toResponseBody("application/json".toMediaType())
        )

        val result = repository.updateProfile("Ana", "Torres", "")

        assertTrue(result.isFailure)
        assertEquals("La contraseña debe tener al menos 8 caracteres", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { sessionDataStore.updateUser(any()) }
    }

    @Test
    fun changePhoneUpdatesLocalSessionPhone() = runTest {
        every { sessionDataStore.userData } returns MutableStateFlow(dto)
        coEvery { service.changePhone(any()) } returns Response.success(Unit)

        val result = repository.changePhone("5599998888")

        assertTrue(result.isSuccess)
        coVerify { sessionDataStore.updateUser(dto.copy(phoneNumber = "5599998888")) }
    }

    @Test
    fun changePhoneServerErrorDoesNotTouchSession() = runTest {
        every { sessionDataStore.userData } returns MutableStateFlow(dto)
        coEvery { service.changePhone(any()) } returns Response.error<Unit>(
            500,
            """{"error":"Error interno"}""".toResponseBody("application/json".toMediaType())
        )

        val result = repository.changePhone("5599998888")

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { sessionDataStore.updateUser(any()) }
    }

    @Test
    fun changePasswordNetworkExceptionReturnsFailure() = runTest {
        coEvery { service.changePassword(any()) } throws java.io.IOException("timeout")

        val result = repository.changePassword("actual123", "nuevaclave1")

        assertTrue(result.isFailure)
        assertEquals("Sin conexión al cambiar la contraseña", result.exceptionOrNull()?.message)
    }

    @Test
    fun getProfileRefreshesSessionData() = runTest {
        coEvery { service.getProfile() } returns Response.success(dto)

        val result = repository.getProfile()

        assertTrue(result.isSuccess)
        assertEquals("Ana", result.getOrNull()?.nombre)
        coVerify { sessionDataStore.updateUser(dto) }
    }
}
