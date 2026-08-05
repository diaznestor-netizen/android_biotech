# Manual Técnico – BioTech Android v1.0.0

## 1. Arquitectura del Sistema
BioTech utiliza una arquitectura **MVVM (Model-View-ViewModel)** bajo principios de **Clean Architecture**, asegurando escalabilidad y facilidad de mantenimiento.

- **Presentación:** Jetpack Compose con Material Design 3.
- **Inyección de Dependencias:** Google Hilt.
- **Red:** Retrofit 2 + OkHttp 4.
- **Persistencia Local:** Room Database (SQLite) + DataStore.
- **Procesamiento en Background:** WorkManager para sincronización robusta.

## 2. Sistema de Diseño (Design System)
Toda la UI está construida sobre la **BioTech UI Library**, ubicada en `presentation/components/`.

### Colores Oficiales
- **PrimaryGreen:** `#22C55E` (Operación exitosa/Stock normal).
- **PrimaryBlue:** `#2563EB` (Identidad corporativa/Acciones principales).
- **PrimaryCyan:** `#06B6D4` (Acento tecnológico/Búsqueda).
- **DarkBackground:** `#0B1220` (Fondo principal modo oscuro).

### Componentes Clave
- `BioTechButton`: Botón con degradado y feedback táctil.
- `BioTechCard`: Contenedor estándar con elevación dinámica.
- `KPIWidget`: Visualización de métricas críticas.
- `StatusBadge`: Indicador de estado semántico (Sync/Stock).

## 3. Sincronización Offline-First
La aplicación implementa una estrategia de sincronización por colas:
1. Las operaciones se registran localmente en `sync_operations`.
2. El `SyncStatusViewModel` monitorea la conectividad.
3. Al detectar conexión, se procesan las peticiones pendientes respetando el orden cronológico y manejando conflictos de versión.

## 4. Configuración de Release
- **Target SDK:** 35 (Android 15).
- **Min SDK:** 26 (Android 8.0).
- **Ofuscación:** R8 activado con reglas personalizadas en `proguard-rules.pro`.
