LOCAL_PATH := $(call my-dir)
LOCAL_CPP_FLAGS := -fno-rtti

include $(CLEAR_VARS)
LOCAL_MODULE    := lib_dvrdata
LOCAL_SRC_FILES := DvrDataNative.c
LOCAL_LDLIBS    := -llog


include $(BUILD_SHARED_LIBRARY)


LOCAL_PATH := $(call my-dir)
LOCAL_CPP_FLAGS := -fno-rtti

include $(CLEAR_VARS)
LOCAL_MODULE    := libcameradata_jni
LOCAL_SRC_FILES := com_wwc2_networks_cmd_jni_CameraDataSync.c
LOCAL_LDLIBS    := -llog


include $(BUILD_SHARED_LIBRARY)
