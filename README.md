# SDLPoP Android

> **Trabajo derivado de [NagyD/SDLPoP](https://github.com/NagyD/SDLPoP)** (GPLv3).
> Este repositorio importa los sources de SDLPoP como snapshot y los compila para Android. No es un fork de GitHub formal porque la historia se inicializó nueva — la relación se documenta aquí. Todo el crédito del juego en sí va al equipo de NagyD; este proyecto solo aporta la capa Android (build system, touch overlay, asset extraction).

Port no oficial de SDLPoP a Android, jugable con controles táctiles.

## Qué es

Este proyecto es un port a Android de [SDLPoP](https://github.com/NagyD/SDLPoP), la reimplementación open source de David Nagy (NagyD) del *Prince of Persia* original creado por Jordan Mechner en 1989. La capa de plataforma se construye sobre SDL2 a través del NDK de Android, reutilizando el código C de SDLPoP prácticamente sin modificaciones.

## Estado actual

- Build de tipo `debug`, sin firmar para distribución en Play Store.
- Soporta Android 5.0 (API 21) en adelante.
- ABIs nativas: `arm64-v8a` y `armeabi-v7a`.
- Controles táctiles básicos mediante un overlay sobre la superficie SDL.
- Los assets del juego (`PRINCE.DAT`, etc.) se extraen al almacenamiento interno en el primer arranque.

## Requisitos del dispositivo

- Android 5.0 (Lollipop) o superior.
- CPU ARM64 (`arm64-v8a`) o ARMv7 (`armeabi-v7a`).
- Aproximadamente 5 MB de espacio libre en el almacenamiento interno.

## Instalación (sideload)

Hay dos formas de instalar el APK no firmado:

1. **Vía adb**, con el dispositivo en modo desarrollador y depuración USB activada:
   ```
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```
2. **Manual**, transfiriendo el APK al dispositivo y abriéndolo desde el explorador de archivos. Necesitas habilitar la opción "Instalar desde fuentes desconocidas" para la app que abra el archivo.

## Build desde source

Requisitos del entorno de build:

- JDK 17.
- Android SDK 35 (platform + build-tools).
- Android NDK 26.1 o superior.
- Gradle 8.7 (incluido vía wrapper).

Pasos:

```
git clone https://github.com/mgrz18/sdlpop-android.git
cd sdlpop-android
echo "sdk.dir=/ruta/a/tu/Android/sdk" > local.properties
./gradlew assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`. El primer build tarda entre 5 y 10 minutos porque compila SDL2, SDL2_image y SDLPoP para ambas ABIs.

## Controles táctiles

El overlay se dibuja encima del render SDL y mapea toques a eventos de teclado equivalentes a los del SDLPoP de PC.

```
+-----------------------------------------------+
|                                          ESC  |
|                                                |
|                                                |
|                                                |
|                                                |
|                                                |
|     ^                                          |
|   < O >                                  SHIFT |
|     v                                          |
+-----------------------------------------------+
```

Mapeo:

- **D-pad** (esquina inferior izquierda): direccionales arriba, abajo, izquierda, derecha.
- **SHIFT** (esquina inferior derecha): acción contextual — agarrar repisa, desenvainar, atacar, recoger objetos.
- **ESC** (esquina superior derecha): pausa y menú del juego.

El overlay soporta multi-touch, así que puedes mantener una dirección mientras presionas SHIFT al mismo tiempo (necesario para correr-saltar agarrando, por ejemplo).

## Arquitectura

A grandes rasgos:

- **SDL2 v2.33** corre como librería nativa estándar de Android, con `SDLActivity` (Java) como punto de entrada.
- **SDL2_image** se enlaza para la decodificación de PNG.
- **SDLPoP** (~32k líneas de C) se compila como `libmain.so`, que `SDLActivity` carga automáticamente al inicio.
- **`PoPActivity`** extiende `SDLActivity` y monta `TouchOverlayView` encima de la superficie de render para inyectar los controles táctiles.
- **Assets**: los archivos de `app/src/main/assets/data/` se copian a `getFilesDir()/data/` en el primer arranque. Un marker file `.assets_extracted_v1` evita repetir la extracción en arranques posteriores.
- **Working directory**: el código C hace `chdir()` al directorio interno de la app para que las rutas relativas que usa SDLPoP (`data/PRINCE.DAT`, `SDLPoP.ini`, etc.) resuelvan correctamente.

## Modificaciones vs upstream

| Archivo | Cambio | Motivo |
|---|---|---|
| `main.c` | Bloque `#ifdef __ANDROID__` con `android_setup_paths()` y log helpers | Hacer `chdir()` al almacenamiento interno para que las rutas relativas de SDLPoP funcionen en Android |

Ningún otro archivo C de SDLPoP fue modificado. El resto de la integración vive en la capa Java/Kotlin y en el build system, no toca el core del juego.

## Roadmap

- Soporte de gamepad bluetooth.
- Configurador de teclas y sensibilidad desde la UI.
- Pulido visual del overlay (animaciones, opacidad ajustable, escalado por densidad).
- Release build firmado.
- Soporte de mods y custom levels desde external storage.
- Save states accesibles desde la UI.

## Créditos

- **Jordan Mechner** — autor original de *Prince of Persia* (1989).
- **David Nagy (NagyD)** — autor y mantenedor de SDLPoP. https://github.com/NagyD/SDLPoP
- **Princed community** — disassembly del juego original y herramientas. https://www.princed.org/
- **SDL team** — Simple DirectMedia Layer. https://www.libsdl.org/

## Licencia

GPLv3, heredada de SDLPoP. El texto completo está en el archivo [`LICENSE`](LICENSE) en la raíz del repositorio.
