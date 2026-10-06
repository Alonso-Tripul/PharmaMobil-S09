# PharmaMobil — Sesión 09: capacidades nativas

**Estudiante:** Rony Alonso Ancajima Tripul  
**Curso:** Desarrollo de Aplicaciones Móviles — UPeU, ciclo VI  
**Docente:** Mg. Reyna Barreto Benjamin David  
**Fecha de la guía:** 6 de octubre de 2026

Implementación de la Guía Práctica de Laboratorio N.º 09 adjunta: moneda peruana mediante `expect/actual` y compartir mediante un contrato de dominio e inyección por plataforma. Incluye detalle de producto en Compose común.

## Abrir y ejecutar Android

1. Descomprime todo el ZIP en una carpeta corta, por ejemplo `C:\Proyectos\Sesion09`.
2. En Android Studio, abre **la carpeta `pharmaMobil`**, donde están `settings.gradle.kts`, `androidApp` y `shared`. No abras el ZIP ni el backend.
3. Usa el JDK integrado de Android Studio compatible con AGP 9 (JDK 17 o superior), instala Android SDK 36 y espera la sincronización de Gradle.
4. Ejecuta PharmaSoft y prepara una categoría y productos con las instrucciones de `../LEEME_PRIMERO.md`.
5. Selecciona la configuración/módulo **`androidApp`** y un emulador Android. Pulsa Run.
6. En la app, abre el menú → Productos. Pulsa un producto → Compartir.

Alternativa en la terminal del proyecto:

```powershell
.\gradlew.bat :androidApp:assembleDebug
.\gradlew.bat :shared:testAndroidHostTest
```

La dirección del backend del emulador es `http://10.0.2.2:8080/api/v1/`. Se configura en `PlatformModule.android.kt`. En un teléfono físico cambia esa dirección por la IP del equipo que ejecuta PharmaSoft. El permiso de Internet está en el manifiesto principal; HTTP local se permite en la variante **debug**.

## Ejecutar iOS

Se necesita macOS, Xcode y un simulador. Abre `iosApp/iosApp.xcodeproj`, selecciona el esquema `iosApp` y ejecuta. El backend predeterminado es `http://localhost:8080/api/v1/`, pensado para el simulador y PharmaSoft en el mismo Mac. La dirección está en `PlatformModule.ios.kt`; una API remota requiere adaptar la dirección y la política de transporte de iOS. `Info.plist` permite conexiones de red locales.

```bash
chmod +x gradlew
./gradlew :shared:iosSimulatorArm64Test
```

## Capacidades nativas

Las rutas siguientes están bajo `shared/src/<sourceSet>/kotlin/pe/edu/upeu/pharmamobil/`.

| Responsabilidad | Código común | Android | iOS |
|---|---|---|---|
| Contrato de moneda | `commonMain/platform/Formato.kt` | `androidMain/platform/Formato.android.kt` | `iosMain/platform/Formato.ios.kt` |
| Contrato de compartir | `commonMain/domain/platform/Compartidor.kt` | `androidMain/platform/CompartidorAndroid.kt` | `iosMain/platform/CompartidorIos.kt` |
| Registro de dependencias | `commonMain/di/AppModule.kt` | `androidMain/di/PlatformModule.android.kt` | `iosMain/di/PlatformModule.ios.kt` |
| Texto compartido | `commonMain/domain/usecase/TextoParaCompartir.kt` | Se recibe el mismo texto común | Se recibe el mismo texto común |
| Formato en presentación | `commonMain/presentation/producto/ProductoUi.kt` | Invoca su `actual` | Invoca su `actual` |
| Detalle y acción | `commonMain/presentation/detalle/DetalleProductoViewModel.kt` y `DetalleProductoScreen.kt` | Selector del sistema | Hoja del sistema |

### 1. Formato de soles

`expect fun formatearSoles(valor: Double): String` define el contrato. Los dos `actual` conservan paquete, nombre, parámetro y retorno. Android usa `NumberFormat` y `Locale("es", "PE")`; iOS usa `NSNumberFormatter`, estilo de moneda y `NSLocale("es_PE")`.

El precio de pantalla se transforma en `Producto.aUi()`. El dominio conserva `precio: Double` y el composable recibe una cadena lista para mostrar. El texto compartido utiliza el formateador, siguiendo el paso 3 de la guía.

El formato exacto lo define cada sistema. No se impone un espacio o símbolo idéntico entre plataformas ni se reconstruyen los decimales a mano.

### 2. Compartir

El contrato `Compartidor` vive en `domain` y solo conoce `String`. Android implementa `ACTION_SEND`, `text/plain`, `EXTRA_TEXT` y un chooser con `FLAG_ACTIVITY_NEW_TASK`, necesario al usar el contexto de aplicación. `MainApplication` suministra ese contexto al iniciar Koin.

iOS crea `UIActivityViewController` en la cola principal. Busca la ventana activa, recorre controladores presentados y configura el popover para iPad. Se conserva el acceso a `keyWindow` como alternativa de compatibilidad. Si no existe una ventana activa no se presenta la hoja; debe comprobarse en ejecución.

Cada `platformModule` registra `single<Compartidor>`. Koin lo inyecta en `DetalleProductoViewModel`. El botón de Compose llama a `compartir()`; la pantalla no usa APIs de Android ni UIKit. Se comparte el producto cargado desde el repositorio; editar campos sin guardarlos no cambia el texto compartido.

### Diferencias por plataforma

| Aspecto | Android | iOS | Decisión |
|---|---|---|---|
| Formateador | `java.text.NumberFormat` | Foundation `NSNumberFormatter` | `expect/actual` para utilidad sin estado |
| Configuración regional | `es`, `PE` | `es_PE` | Usar moneda peruana en ambas |
| Símbolos y espacios | Dependen de los datos regionales de Android | Dependen de Foundation y versión del sistema | Comparar capturas reales, sin asumir igualdad literal |
| Compartir texto | Intent y selector | `UIActivityViewController` | Contrato e inyección para sustituir la capacidad en pruebas |
| Dependencia nativa | `Context` de aplicación | Ventana/controlador activo | Mantener las dependencias en sus source sets |
| Condición de presentación | Bandera `NEW_TASK` | Cola principal y popover en iPad | Evitar fallos de presentación |
| Motor HTTP | Ktor Android (HttpURLConnection) | Ktor Darwin | Red común con motor por plataforma |
| Equipo de compilación | Android Studio en Windows/macOS/Linux | macOS y Xcode | Verificación independiente |

### Interoperabilidad Kotlin–Swift

`iOSApp.swift` importa `Shared` y llama a `KoinIosKt.doInitKoinIos()`. El archivo Kotlin `KoinIos.kt` agrupa su función superior bajo el sufijo `Kt`; la exportación adapta el nombre que empieza por `init`. `ContentView.swift` llama a `MainViewControllerKt.MainViewController()` y aloja la UI Compose mediante `UIViewControllerRepresentable`.

Los tipos Kotlin anulables se utilizan como opcionales del lado Swift. Las clases Kotlin no se convierten automáticamente en estructuras de valor de Swift; no debe suponerse que su sintaxis de `copy` y desestructuración se conserva. Una jerarquía sellada no aporta la misma comprobación exhaustiva al `switch` de Swift. Las funciones `suspend` se exportan mediante puentes de finalización y pueden usarse como `async` en Swift; la propagación de excepciones debe diseñarse con `@Throws` cuando corresponda. Esta práctica mantiene estados y decisiones en Kotlin y no modifica la entrada Swift existente.

## Conexión con PharmaSoft

El proyecto adjunto originalmente tenía repositorios en memoria. Se añadió `ProductoRepositorioRest` para los productos y se registró como repositorio de ejecución. Consume el contrato real incluido: `GET /productos` devuelve `contenido` y `ultima`; el cliente recorre todas las páginas. Se usan también `GET /productos/{id}`, `POST`, `PUT` y `DELETE`.

Crear/editar envía `nombre`, `precio`, `stock`, `estado` y `categoriaId`. El ID de categoría debe existir en PharmaSoft. La baja es **lógica**: el backend cambia `estado=false` y el producto puede seguir en el listado con la etiqueta Inactivo. El módulo Clientes conserva el repositorio en memoria de la base adjunta. JWT y sincronización offline pertenecen al producto de la unidad y no se presentan como implementados en esta sesión.

### Backend con el esquema existente usado en la prueba

El primer arranque conectó a Oracle, pero Flyway detectó una diferencia entre la migración V1 local y la que ya estaba registrada. Para esta sesión se inició el backend desde la carpeta `pharmaSoft` con:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=sesion09" "-Dspring-boot.run.arguments=--spring.flyway.enabled=false --spring.jpa.hibernate.ddl-auto=validate"
```

Este comando usa el esquema existente y mantiene la validación de Hibernate. La discrepancia del historial de Flyway sigue pendiente de revisar contra la migración que creó esas tablas; no se hizo `repair`, ni se recreó la base. En una base nueva debe usarse una migración compatible con su versión de Oracle.

## Evidencias de la práctica

Consulta `docs/EVIDENCIAS_S09.md`. Deben adjuntarse cinco capturas auténticas:

1. Error por falta de `actual` del punto de control 1.
2. Listado Android con precios en soles.
3. Listado iOS con precios en soles.
4. Selector de compartir Android con el texto del producto.
5. Hoja de compartir iOS con el mismo producto.

**Resultados verificados el 06/10/2026 en el equipo del estudiante:**

- `:androidApp:assembleDebug` y `:shared:testAndroidHostTest`: **BUILD SUCCESSFUL**.
- Informe de Gradle: **54 pruebas, 0 fallos, 0 omitidas y 100 % de éxito**; duración de las pruebas **0,664 s**. Son pruebas de host; no son 54 pruebas de interfaz en el emulador.
- Pixel 6, Android API 34: inventario obtenido de PharmaSoft, con Paracetamol 500 mg a **S/ 6.00** y stock **25**.
- Detalle y selector nativo Android verificados. Texto observado: `Paracetamol 500 mg — S/ 6.00 · Stock: 25`.
- Punto de control: al retirar temporalmente `Formato.android.kt`, el compilador informó `Expected formatearSoles has no actual declaration`. Después de restaurarlo, `:shared:compileAndroidMain` terminó con **BUILD SUCCESSFUL**; la tarea de compilación se recuperó de caché.
- PharmaSoft inició en el puerto 8080 con Java 21 y Oracle **21.3 / XEPDB1**, usando las tablas existentes y Flyway desactivado solo mediante un argumento de ejecución.
- **iOS: compilación, ejecución, listado y hoja de compartir pendientes de una Mac con Xcode.** Hay implementación en el código, pero aún no hay evidencia de ejecución iOS. Las capturas Android no sustituyen las dos capturas iOS exigidas.

Las capturas originales están incluidas y comentadas en [docs/EVIDENCIAS_S09.md](docs/EVIDENCIAS_S09.md). La prueba visual acredita carga, formato, detalle y apertura del selector; no se declara verificado todo el CRUD ni el envío a un destinatario.

La revisión de source sets encontró las tres firmas de `formatearSoles`, los dos registros de `Compartidor`, el botón en el detalle y ninguna importación `android.*` o `platform.UIKit.*` en `presentation`.

Se conservaron las pruebas originales y se añadieron cinco casos para compartir el producto persistido, evitar compartir un producto anterior después de un error, mostrar errores del selector, recorrer la paginación de PharmaSoft y propagar un 404. Las pruebas usan dobles de dominio y `MockEngine`; no sustituyen la evidencia de los selectores nativos.

## Rama y commits exigidos

Trabajo individual de Rony Alonso Ancajima Tripul.

- Repositorio: [Alonso-Tripul/PharmaMobil-S09](https://github.com/Alonso-Tripul/PharmaMobil-S09).
- Rama publicada: [feature/expect-actual-ancajima](https://github.com/Alonso-Tripul/PharmaMobil-S09/tree/feature/expect-actual-ancajima).
- Historial de la rama: [commits](https://github.com/Alonso-Tripul/PharmaMobil-S09/commits/feature/expect-actual-ancajima).
- Commit de corrección de la prueba REST mostrado en consola: `c9abcbb`.

La consola confirmó el envío de la rama y de las correcciones. El historial completo debe comprobarse para acreditar los tres commits propios que exige la guía; no se deduce su cantidad del número de tareas de Gradle.

## Referencias técnicas

- Guía Práctica de Laboratorio N.º 09 adjunta, páginas 5–11.
- JetBrains: https://kotlinlang.org/docs/multiplatform/multiplatform-expect-actual.html
- JetBrains: https://kotlinlang.org/docs/native-objc-interop.html
- Ktor: https://ktor.io/docs/client-dependencies.html
- Ktor: https://ktor.io/docs/client-serialization.html
- Koin: https://insert-koin.io/docs/reference/koin-core/dsl/
- Apple: https://developer.apple.com/documentation/uikit/uipopoverpresentationcontroller/sourceview

## Código específico de plataforma

Actividad Autónoma N.º 09. Inventario completo: [INVENTARIO.md](docs/autonoma-s09/INVENTARIO.md). Análisis comparativo: [ANALISIS_COMPARATIVO.md](docs/autonoma-s09/ANALISIS_COMPARATIVO.md). Evidencias: [EVIDENCIAS.md](docs/autonoma-s09/EVIDENCIAS.md).

| Capacidad | Contrato | Android | iOS |
|---|---|---|---|
| Moneda peruana | `expect fun formatearSoles(valor: Double): String` | `NumberFormat` y `Locale("es", "PE")` | `NSNumberFormatter` y `NSLocale("es_PE")` |
| Compartir | Interfaz común `Compartidor`; proveedor mediante `actual platformModule` | `Context`, `Intent.ACTION_SEND` y chooser | `UIActivityViewController` y controlador activo |
| Inyección | `expect val platformModule: Module` | Koin `module`, `androidContext()`, motor Ktor Android | Koin `module`, motor Ktor Darwin |
| Información del dispositivo | `expect class InfoDispositivo()`; `sistema` y `version` | `Build.VERSION.RELEASE` | `UIDevice.currentDevice.systemName` y `systemVersion` |

En la app, abrir **menú → Acerca de**. La pantalla común está en `presentation/acerca/AcercaDeScreen.kt`; las rutas completas de los contratos e implementaciones figuran en el inventario. No hay importaciones `android.` ni `platform.` en todo commonMain.

**Verificado en Android:** la compilación y la tarea de pruebas finalizaron correctamente; Acerca de mostró **Android 14**. La captura y el log se incluyen en las evidencias. **iOS:** código implementado, compilación y evidencia visual pendientes. La actividad no se presenta como verificada en ambas plataformas hasta completar esa ejecución.
