# Tareas de Implementación: NotificationCenter BioTech

## 1. Núcleo de Notificaciones (Domain)
- [x] Definir Modelos (`NotificationPriority`, `NotificationChannel`, `NotificationEvent`)
- [x] Implementar `NotificationFormatter` (Plantillas Markdown/Emoji)
- [x] Implementar `NotificationDispatcher` (Ruteo por canales)
- [x] Implementar `NotificationCenter` (Punto de entrada único)

## 2. Infraestructura (Data)
- [x] Refinar `NotificationRepository` y `NotificationRepositoryImpl`
- [x] Asegurar ruteo HTTPS seguro hacia el backend Go

## 3. Integración de Módulos
- [x] Integrar en **Incidencias** (Creación/Estado/Cierre)
- [x] Integrar en **Maquinaria** (Fuera de servicio/Fallas)
- [x] Integrar en **Inventario** (Stock Bajo/Crítico/Agotado)
- [x] Integrar en **Proyectos** (Creación/Retrasos/Prioridad)
- [x] Integrar en **Reportes** (Generación/Exportación)
- [x] Integrar en **Seguridad** (Accesos/Bloqueos/Contraseñas)

## 5. Infraestructura de Telegram y Autenticación OTP (v1.2.0)
- [x] Fase 1: Vinculación con Telegram (Android UI y Servicios)
- [x] Fase 2: Implementación de Sistema OTP (Pantalla y Temporizador)
- [x] Fase 3: Lógica de Re-autenticación cada 4 horas
- [x] Fase 4: Integración de hitos de seguridad en NotificationCenter
- [/] Fase 5: Pruebas de integración completa (Linking -> OTP -> Sync)
