# Evidencias reales — Sesión 09

Estas evidencias deben obtenerse ejecutando la entrega. La carpeta `evidencias-s09` no contiene capturas de ejecución, porque no se dispone de emuladores ni de macOS en el entorno de preparación.

| Orden | Nombre de archivo sugerido | Qué debe verse | Estado |
|---|---|---|---|
| 1 | `01_error_falta_actual.png` | Compilador reclamando el `actual` del formateador | Pendiente |
| 2 | `02_android_listado_soles.png` | Producto y precio con moneda peruana | Pendiente |
| 3 | `03_ios_listado_soles.png` | El mismo producto y precio en iOS | Pendiente |
| 4 | `04_android_compartir.png` | Selector Android y texto compartido | Pendiente |
| 5 | `05_ios_compartir.png` | Hoja iOS y texto compartido | Pendiente |

## Punto de control 1 sin perder la implementación

1. Trabaja en una copia temporal del proyecto y cierra cualquier compilación previa.
2. Cambia el nombre de `Formato.android.kt` a `Formato.android.kt.txt`, y de `Formato.ios.kt` a `Formato.ios.kt.txt`, conservando `commonMain/platform/Formato.kt`.
3. Ejecuta `gradlew.bat :androidApp:assembleDebug` en Windows, o `./gradlew :androidApp:assembleDebug` en macOS/Linux.
4. Captura el error **del compilador Kotlin** por ausencia de `actual`. Un error de red o de SDK no es esa evidencia. La compilación Android evidencia su target; para evidenciar iOS compila su framework también en el Mac.
5. Restituye los dos nombres `.kt` y recompila. No entregues el proyecto con los `actual` retirados.

## Capturas en los dispositivos

Usa el mismo producto, por ejemplo Paracetamol, precio 12.50 y stock 8. Primero captura el listado. Abre el detalle, pulsa Compartir y captura la hoja/selector. Si el selector no muestra el texto completo, abre un destino como Notas o un borrador para mostrarlo; no hace falta enviar el texto a otra persona. Registra las versiones reales de Android e iOS y comenta el símbolo, los espacios y los decimales que observas.

## Completar al ejecutar

- Dispositivo/emulador Android y versión: pendiente.
- Simulador/dispositivo iOS y versión: pendiente.
- Resultado real de compilación Android: pendiente.
- Resultado real de compilación iOS: pendiente.
- Resultado de pruebas y cantidad que aprobó: pendiente.
- Diferencias de formato observadas: pendiente de capturas.
- Enlace a rama del estudiante: pendiente.
- Enlace a rama del segundo integrante, si corresponde: pendiente.
- Tres commits propios por integrante: pendiente de integración en sus repositorios.

La actividad autónoma asociada tiene fecha límite indicada en la guía: 12/10/2026 a las 23:59. Este documento prepara la comparación, pero las observaciones empíricas y las imágenes solo se completan después de ejecutar.
