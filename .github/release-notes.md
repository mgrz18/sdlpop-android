## SDLPoP Android — primer release

Port a Android del clásico Prince of Persia (1989) basado en [SDLPoP](https://github.com/NagyD/SDLPoP) de NagyD.

### Instalación

1. Descarga el APK adjunto (`sdlpop-android-v1.0.0-debug.apk`).
2. Habilita "Instalar apps desconocidas" para tu navegador o explorador de archivos.
3. Abre el APK y confirma la instalación.
4. Soporta Android 5.0+ (API 21), arquitecturas `arm64-v8a` y `armeabi-v7a`.

### Funcionalidad

**Touch overlay con look de teclado retro:**
- D-pad estilo PC en T invertida con teclas diagonales (`↖ ↗`) para saltos largos entre plataformas.
- Tecla `SHIFT/ACTION` (agarrar, atacar, sacar espada).
- Tecla `ESC` para abrir el menú in-game.
- Tecla `ENTER` aparece dinámicamente solo cuando el menú está abierto, para confirmar opciones.
- Animación de hundir tecla al presionar (bevel invertido).
- Translucidez ajustada para no obstruir la vista del juego.

**Cheats integrados:**
- Habilita los cheats desde `Settings → Enable cheats` en el menú interno de SDLPoP.
- Aparece un botón `CHEATS` en el overlay que despliega un panel con 10 cheats útiles: pasar de nivel, big potion, heal, feather fall, invertir, ciego, resucitar, matar guardia, sumar/restar tiempo.

**Otros:**
- Triple tap en zona vacía de la pantalla muestra el tiempo restante (equivalente a la barra espaciadora).
- Orientación forzada landscape.
- Pantalla completa inmersiva (sin barras de sistema).
- Compatible con notch / display cutouts.
- Asset extraction al primer arranque (PRINCE.DAT y demás archivos del juego original ya incluidos).

### Limitaciones conocidas

- Build sin firmar (debug). Para uso prolongado tendrás que reinstalar al expirar el debug keystore (1 año).
- No hay soporte UI para gamepads bluetooth (SDL2 los detecta pero no hay configurador).
- Save states se guardan, pero no hay UI dedicada (usa el menú nativo de SDLPoP).

### Créditos

- **Jordan Mechner** — autor original de Prince of Persia (1989).
- **David Nagy (NagyD)** y la [Princed community](https://www.princed.org/) — port SDLPoP y herramientas.
- **SDL team** — Simple DirectMedia Layer.

### Licencia

GPLv3, heredada de SDLPoP. Ver `LICENSE` en el repositorio.
