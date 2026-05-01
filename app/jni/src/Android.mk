LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := main

SDL_PATH := ../SDL
SDL_IMAGE_PATH := ../SDL2_image

LOCAL_C_INCLUDES := \
    $(LOCAL_PATH)/$(SDL_PATH)/include \
    $(LOCAL_PATH)/$(SDL_PATH)/include_compat \
    $(LOCAL_PATH)/$(SDL_IMAGE_PATH)

LOCAL_SRC_FILES := \
    sdlpop/main.c \
    sdlpop/data.c \
    sdlpop/seg000.c \
    sdlpop/seg001.c \
    sdlpop/seg002.c \
    sdlpop/seg003.c \
    sdlpop/seg004.c \
    sdlpop/seg005.c \
    sdlpop/seg006.c \
    sdlpop/seg007.c \
    sdlpop/seg008.c \
    sdlpop/seg009.c \
    sdlpop/seqtbl.c \
    sdlpop/replay.c \
    sdlpop/options.c \
    sdlpop/lighting.c \
    sdlpop/screenshot.c \
    sdlpop/menu.c \
    sdlpop/midi.c \
    sdlpop/opl3.c \
    sdlpop/stb_vorbis.c

LOCAL_CFLAGS := -std=c99 \
    -Wno-unused-result -Wno-macro-redefined -Wno-pointer-sign \
    -Wno-format

LOCAL_SHARED_LIBRARIES := SDL2 SDL2_image

LOCAL_LDLIBS := -lGLESv1_CM -lGLESv2 -lOpenSLES -llog -landroid

include $(BUILD_SHARED_LIBRARY)
