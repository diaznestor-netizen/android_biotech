package com.biobox.biotech.presentation.profile

import com.biobox.biotech.domain.model.User
import com.biobox.biotech.domain.model.UserRole
import com.biobox.biotech.domain.repository.AuthRepository
import com.biobox.biotech.domain.repository.ProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val profileRepository = mockk<ProfileRepository>(relaxed = true)
    private val authRepository = mockk<AuthRepository>()
    private val user = User("1", "Ana", "Torres", "ana@example.com", UserRole.SUPERVISOR)

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        every { authRepository.currentUser } returns MutableStateFlow(user)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createVm() = ProfileViewModel(profileRepository, authRepository)

    private fun TestScope.collectEvents(vm: ProfileViewModel): MutableList<String> {
        val events = mutableListOf<String>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.operationEvents.collect { events.add(it) }
        }
        return events
    }

    @Test
    fun exposesCurrentUserFromSession() = runTest {
        val vm = createVm()
        advanceUntilIdle()
        assertEquals(user, vm.profile.value)
    }

    @Test
    fun saveProfileRejectsEmptyNameWithoutCallingRepository() = runTest {
        val vm = createVm()
        val events = collectEvents(vm)
        vm.saveProfile(" ", "Torres", "ana@example.com")
        advanceUntilIdle()
        assertTrue(events.contains("Nombre y apellido son obligatorios"))
        coVerify(exactly = 0) { profileRepository.updateProfile(any(), any(), any()) }
    }

    @Test
    fun saveProfileRejectsInvalidEmailWithoutCallingRepository() = runTest {
        val vm = createVm()
        val events = collectEvents(vm)
        vm.saveProfile("Ana", "Torres", "no-es-un-email")
        advanceUntilIdle()
        assertTrue(events.contains("El formato del email no es válido"))
        coVerify(exactly = 0) { profileRepository.updateProfile(any(), any(), any()) }
    }

    @Test
    fun saveProfileSuccessExitsEditModeAndEmitsConfirmation() = runTest {
        coEvery { profileRepository.updateProfile("Ana", "Torres", "ana@example.com") } returns Result.success(user)
        val vm = createVm()
        val events = collectEvents(vm)
        vm.startEdit()
        assertTrue(vm.editMode.value)
        vm.saveProfile("Ana", "Torres", "ana@example.com")
        advanceUntilIdle()
        assertFalse(vm.editMode.value)
        assertFalse(vm.saving.value)
        assertTrue(events.contains("Perfil actualizado"))
    }

    @Test
    fun saveProfileFailureKeepsEditModeAndEmitsError() = runTest {
        coEvery { profileRepository.updateProfile(any(), any(), any()) } returns Result.failure(Exception("Error HTTP 500"))
        val vm = createVm()
        val events = collectEvents(vm)
        vm.startEdit()
        vm.saveProfile("Ana", "Torres", "ana@example.com")
        advanceUntilIdle()
        assertTrue(vm.editMode.value)
        assertTrue(events.contains("Error HTTP 500"))
    }

    @Test
    fun changePasswordRejectsShortPassword() = runTest {
        val vm = createVm()
        val events = collectEvents(vm)
        vm.changePassword("actual123", "corta", "corta")
        advanceUntilIdle()
        assertTrue(events.any { it.contains("al menos 8 caracteres") })
        coVerify(exactly = 0) { profileRepository.changePassword(any(), any()) }
    }

    @Test
    fun changePasswordRejectsMismatchedConfirmation() = runTest {
        val vm = createVm()
        val events = collectEvents(vm)
        vm.changePassword("actual123", "nuevaclave1", "otraclave1")
        advanceUntilIdle()
        assertTrue(events.contains("Las contraseñas no coinciden"))
        coVerify(exactly = 0) { profileRepository.changePassword(any(), any()) }
    }

    @Test
    fun changePasswordSuccessInvokesOnSuccessAndEmitsConfirmation() = runTest {
        coEvery { profileRepository.changePassword("actual123", "nuevaclave1") } returns Result.success(Unit)
        val vm = createVm()
        val events = collectEvents(vm)
        var successCalled = false
        vm.changePassword("actual123", "nuevaclave1", "nuevaclave1") { successCalled = true }
        advanceUntilIdle()
        assertTrue(successCalled)
        assertTrue(events.contains("Contraseña actualizada"))
    }

    @Test
    fun changePhoneNormalizesDigitsBeforeCallingRepository() = runTest {
        coEvery { profileRepository.changePhone("5512345678") } returns Result.success(Unit)
        val vm = createVm()
        val events = collectEvents(vm)
        vm.changePhone("(55) 1234-5678")
        advanceUntilIdle()
        coVerify { profileRepository.changePhone("5512345678") }
        assertTrue(events.contains("Teléfono actualizado"))
    }

    @Test
    fun changePhoneRejectsInvalidLength() = runTest {
        val vm = createVm()
        val events = collectEvents(vm)
        vm.changePhone("123")
        advanceUntilIdle()
        assertTrue(events.contains("El teléfono debe tener 10 dígitos"))
        coVerify(exactly = 0) { profileRepository.changePhone(any()) }
    }
}
