/*
SDLPoP, a port/conversion of the DOS game Prince of Persia.
Copyright (C) 2013-2025  Dávid Nagy

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

The authors of this program may be contacted at https://forum.princed.org
*/

#include "common.h"

#ifdef __ANDROID__
#include <SDL.h>
#include <unistd.h>
#include <jni.h>
#include <android/log.h>
#define POP_LOG(...) __android_log_print(ANDROID_LOG_INFO, "SDLPoP", __VA_ARGS__)

JNIEXPORT jboolean JNICALL
Java_com_mgrz18_sdlpop_PoPNative_isMenuShown(JNIEnv *env, jclass clazz) {
    (void)env; (void)clazz;
    return is_menu_shown != 0 ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_mgrz18_sdlpop_PoPNative_isCheatsEnabled(JNIEnv *env, jclass clazz) {
    (void)env; (void)clazz;
    return cheats_enabled != 0 ? JNI_TRUE : JNI_FALSE;
}
static void android_setup_paths(void) {
    const char *base = SDL_AndroidGetInternalStoragePath();
    if (base != NULL) {
        if (chdir(base) == 0) {
            POP_LOG("chdir to internal storage: %s", base);
        } else {
            POP_LOG("chdir FAILED for: %s", base);
        }
    } else {
        POP_LOG("SDL_AndroidGetInternalStoragePath returned NULL");
    }
}

static void android_disable_accelerometer_joystick(void) {
    SDL_SetHint(SDL_HINT_ACCELEROMETER_AS_JOYSTICK, "0");
}
#endif

#ifdef __amigaos4__
static const char version[] = "\0$VER: SDLPoP " SDLPOP_VERSION " (" __AMIGADATE__ ")";
static const char stack[] = "$STACK:200000";
#endif

#ifdef __PSP__
#include <psppower.h>
#endif

int main(int argc, char *argv[])
{
#ifdef __ANDROID__
    android_setup_paths();
    android_disable_accelerometer_joystick();
#endif
	#ifdef __PSP__
	scePowerSetClockFrequency(333,333,166);
	#endif
	g_argc = argc;
	g_argv = argv;
	pop_main();
	return 0;
}

