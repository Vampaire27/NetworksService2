#include <jni.h>
#include <string.h>
#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include "com_wwc2_networks_cmd_jni_CameraDataSync.h"
#include <android/log.h>
#include<stdio.h>
#include <errno.h>
#include <signal.h>

static const char *TAG = "CameraDataSync";

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

int fd;
/*
 * Class:     com_wwc2_networks_cmd_jni_CameraDataSync
 * Method:    openDev
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_wwc2_networks_cmd_jni_CameraDataSync_openDev
  (JNIEnv *env, jobject obj, jstring devPath){
    jboolean is_copy = JNI_FALSE;
    LOGD("---openDev()");
    const char *path_utf = (*env)->GetStringUTFChars(env, devPath, &is_copy);

    if (path_utf == NULL) {
             LOGD("---openDev() path_utf ==null");
            (*env)->ReleaseStringUTFChars(env, devPath, path_utf);
            return JNI_FALSE; /* OutOfMemoryError already thrown */
     }
     if(fd == 0){
          fd = open(path_utf, O_CREAT | O_RDWR, 0666);
     }else{
         LOGD(" openDev fd has been open" );
     }

     (*env)->ReleaseStringUTFChars(env, devPath, path_utf);

    return JNI_TRUE;
  }

/*
 * Class:     com_wwc2_networks_cmd_jni_CameraDataSync
 * Method:    closeDev
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_wwc2_networks_cmd_jni_CameraDataSync_closeDev
  (JNIEnv *env, jobject obj, jstring devPath){
      LOGD("---closeDev()");
      if(fd > 0){
          close(fd);
          fd = 0;
      }
      return JNI_TRUE;
  }

/*
 * Class:     com_wwc2_networks_cmd_jni_CameraDataSync
 * Method:    blockRead
 * Signature: ()I
 */
JNIEXPORT jbyte JNICALL Java_com_wwc2_networks_cmd_jni_CameraDataSync_blockRead
  (JNIEnv *env, jobject obj){
      jbyte data;
      read(fd, &data, 1);
     return data;
  }

/*
 * Class:     com_wwc2_networks_cmd_jni_CameraDataSync
 * Method:    blockWrite
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_wwc2_networks_cmd_jni_CameraDataSync_blockWrite
  (JNIEnv *env, jobject obj){
    jint data =1;
    //write(fd, &data, 1);
    return data;
  }

