# Walkthrough: Infraestructura de Telegram y Autenticación OTP v1.2.0

Se ha completado la implementación del sistema de seguridad reforzada, vinculando BioTech con Telegram para la entrega de alertas y verificación de identidad (OTP).

## 1. Vinculación con Telegram
Se ha añadido una nueva sección en el **Perfil de Usuario** para gestionar la conexión con Telegram.
- **Flujo de Vinculación:** El usuario solicita un código en la App, lo envía al bot oficial, y el backend asocia automáticamente el `chat_id`.
- **Indicadores de Estado:** Badge visual (🟢 Vinculado / 🔴 No vinculado) con información del usuario de Telegram y última sincronización.

## 2. Sistema OTP de Seguridad
Implementación de un desafío de seguridad basado en códigos de un solo uso.
- **Pantalla de Verificación:** Diseño moderno para el ingreso de 6 dígitos con teclado numérico optimizado.
- **Temporizador de Expiración:** Cuenta regresiva de 5 minutos y lógica de re-envío protegida.
- **Firma de Acceso:** Una vez validado el OTP, se actualiza el timestamp de confianza en el dispositivo.

## 3. Re-autenticación Obligatoria (4 Horas)
Política de seguridad industrial para prevenir accesos no autorizados en dispositivos compartidos.
- **Bloqueo Automático:** Cada 4 horas de actividad, la aplicación intercepta la navegación y redirige al usuario a la pantalla de validación OTP.
- **Persistencia de Contexto:** El bloqueo no destruye los datos en memoria, permitiendo retomar el trabajo exactamente donde se dejó tras ingresar el código.

## 4. Notificaciones de Seguridad
El **NotificationCenter** ahora reporta hitos críticos:
- **Alertas de OTP:** Notifica cuando se envía un código o cuando se valida un acceso.
- **Gestión de Cuentas:** Alerta sobre nuevas vinculaciones de Telegram o bloqueos preventivos por fallos reiterados.

## Notas Técnicas
> [!IMPORTANT]
> **Seguridad de Tokens:** Android no almacena el Token del Bot ni Chat IDs. Toda la comunicación se realiza vía HTTPS hacia el backend en Go, quien actúa como guardián de las credenciales de Telegram.
> **R8 Stability:** Se han actualizado las reglas de ProGuard para asegurar que las nuevas clases de respuesta (`TelegramStatusResponse`, etc.) no sean ofuscadas.

## Próximos Pasos
- Validar el Webhook en el backend Go para procesar el mensaje `/start`.
- Iniciar el despliegue de las nuevas alertas operativas (Stock Crítico, Incidencias) a los usuarios vinculados.
