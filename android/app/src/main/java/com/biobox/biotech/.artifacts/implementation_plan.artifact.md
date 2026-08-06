# Autenticación Reforzada y Vinculación con Telegram – BioTech v1.2.0

Este plan detalla la implementación del flujo de seguridad basado en Telegram, incluyendo la vinculación de cuentas, el sistema de verificación OTP (One-Time Password) y la política de re-autenticación obligatoria cada 4 horas.

## User Review Required

> [!IMPORTANT]
> **Interrupción de Sesión:** Cada 4 horas, la aplicación bloqueará el acceso y solicitará un código OTP enviado por Telegram. Los datos no guardados se mantendrán en memoria, pero no se permitirá la navegación hasta validar el código.
> **Dependencia de Telegram:** El uso de BioTech v1.2.0 requerirá obligatoriamente una cuenta de Telegram vinculada para cumplir con los estándares de seguridad industrial.

## Proposed Changes

### 1. Gestión de Sesión y Seguridad (Core)

#### [MODIFY] [SessionDataStore.kt](file:///C:/Users/Nestor Gaona/Desktop/android/android/app/src/main/java/com/biobox/biotech/core/datastore/SessionDataStore.kt)
- Añadir persistencia para `lastReAuthTime` (Long) y `isTelegramLinked` (Boolean).

#### [MODIFY] [AuthRepository.kt](file:///C:/Users/Nestor Gaona/Desktop/android/android/app/src/main/java/com/biobox/biotech/domain/repository/AuthRepository.kt) & [AuthRepositoryImpl.kt](file:///C:/Users/Nestor Gaona/Desktop/android/android/app/src/main/java/com/biobox/biotech/data/repository/AuthRepositoryImpl.kt)
- Implementar `requestOtp()`: Solicita al backend Go generar y enviar un código de 6 dígitos.
- Implementar `verifyOtp(code)`: Valida el código y actualiza `lastReAuthTime`.
- Implementar `getTelegramStatus()`: Consulta el estado de vinculación y nombre de usuario.

### 2. Interfaz de Usuario (UI/UX)

#### [MODIFY] [NavRoutes.kt](file:///C:/Users/Nestor Gaona/Desktop/android/android/app/src/main/java/com/biobox/biotech/presentation/navigation/NavRoutes.kt)
- Añadir rutas: `TelegramLinking` y `OtpVerification`.

#### [MODIFY] [ProfileScreen.kt](file:///C:/Users/Nestor Gaona/Desktop/android/android/app/src/main/java/com/biobox/biotech/presentation/profile/ProfileScreen.kt)
- Integrar sección de **Telegram** con badges de estado (🟢 Vinculado / 🔴 No vinculado).
- Botón para iniciar el flujo de vinculación.

#### [NEW] [TelegramLinkingScreen.kt](file:///C:/Users/Nestor Gaona/Desktop/android/android/app/src/main/java/com/biobox/biotech/presentation/profile/TelegramLinkingScreen.kt)
- Muestra el código único de vinculación e instrucciones para el bot.

#### [NEW] [OtpVerificationScreen.kt](file:///C:/Users/Nestor Gaona/Desktop/android/android/app/src/main/java/com/biobox/biotech/presentation/auth/OtpVerificationScreen.kt)
- Entrada de 6 dígitos con teclado numérico.
- Temporizador de 5 minutos y lógica de re-envío.

### 3. Integración con NotificationCenter

#### [MODIFY] [NotificationModels.kt](file:///C:/Users/Nestor Gaona/Desktop/android/android/app/src/main/java/com/biobox/biotech/domain/notifications/NotificationModels.kt)
- Añadir eventos: `TelegramLinked`, `OtpSent`, `OtpVerified`, `AuthExpired`.

### 4. Flujo de Re-autenticación (BioTechNav)

#### [MODIFY] [BioTechNav.kt](file:///C:/Users/Nestor Gaona/Desktop/android/android/app/src/main/java/com/biobox/biotech/presentation/navigation/BioTechNav.kt)
- Implementar un interceptor de navegación que verifique si `currentTime - lastReAuthTime > 4h`.
- Redirección automática a `OtpVerificationScreen` si la sesión ha expirado.

---

## Verification Plan

### Automated Tests
- Pruebas unitarias en `AuthViewModel` para validar el cálculo de expiración de 4 horas.
- Tests de integración para el flujo de guardado y recuperación de `lastReAuthTime`.

### Manual Verification
1. Vincular cuenta desde el Perfil -> Verificar estado 🟢 en la App.
2. Forzar expiración (cambiando hora del sistema) -> Confirmar bloqueo de UI.
3. Solicitar OTP -> Recibir en Telegram -> Ingresar en Android -> Recuperar acceso.

## Criterios de Aceptación
- El usuario no puede operar la app si la sesión de 4 horas ha expirado.
- El código OTP expira estrictamente a los 5 minutos (backend-side).
- Los mensajes de Telegram llegan con el formato corporativo de BioTech.
