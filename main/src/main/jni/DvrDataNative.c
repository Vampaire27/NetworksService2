#include <jni.h>
#include <string.h>
#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include "com_wwc2_networks_server_dvr_jni_DvrDataNative.h"
#include <android/log.h>
#include<sys/mman.h>
#include<stdio.h>
#include <errno.h>
#include <signal.h>

static const char *TAG = "dvrDataNative";

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

int fd;
void* mainData = NULL;
void* subData = NULL;

JNIEXPORT void JNICALL Java_com_wwc2_networks_server_dvr_jni_DvrDataNative_openMain
    (JNIEnv *env, jclass obj, jstring devPath) {
    /* Opening device */
    {
        LOGD("--jni--openMain()");

        jboolean iscopy;
        const char *path_utf = (*env)->GetStringUTFChars(env, devPath, &iscopy);

        fd = open(path_utf, O_CREAT | O_RDWR, 0666);
        LOGD("openMain() fd = %d", fd);

        if(ftruncate(fd, 26*0x1000) < 0)
        {
            LOGD("main ftruncate fail\n");
            if(fd > 0)
                close(fd);
            fd = 0;
        }

        (*env)->ReleaseStringUTFChars(env, devPath, path_utf);
        if (fd == -1) {
            /* Throw an exception */
            LOGE("main Cannot open port");
            /* TODO: throw an exception */
            //return NULL;
        }

        mainData = mmap(NULL, 26*0x1000, (PROT_READ | PROT_WRITE), MAP_SHARED, fd, 0);
        *((unsigned int*)mainData+(25*0x1000/sizeof(unsigned int))) = 0;

        close(fd);

        if(mainData == (void*)-1)
        {
            LOGD("main mmap fail\n");
        }
    }
}

JNIEXPORT void JNICALL Java_com_wwc2_networks_server_dvr_jni_DvrDataNative_openSub
    (JNIEnv *env, jclass obj, jstring devPath) {
    /* Opening device */
    {
        LOGD("--jni--openSub()");

        jboolean iscopy;
        const char *path_utf = (*env)->GetStringUTFChars(env, devPath, &iscopy);

        fd = open(path_utf, O_CREAT | O_RDWR, 0666);
        LOGD("openSub() fd = %d", fd);

        if(ftruncate(fd, 26*0x1000) < 0)
        {
            LOGD("sub ftruncate fail\n");
            if(fd > 0)
                close(fd);
            fd = 0;
        }

        (*env)->ReleaseStringUTFChars(env, devPath, path_utf);
        if (fd == -1) {
            /* Throw an exception */
            LOGE("sub Cannot open port");
            /* TODO: throw an exception */
            //return NULL;
        }

        subData = mmap(NULL, 26*0x1000, (PROT_READ | PROT_WRITE), MAP_SHARED, fd, 0);
        *((unsigned int*)subData+(25*0x1000/sizeof(unsigned int))) = 0;

        close(fd);

        if(subData == (void*)-1)
        {
            LOGD("sub mmap fail\n");
        }
    }
}

JNIEXPORT void JNICALL Java_com_wwc2_networks_server_dvr_jni_DvrDataNative_closeMain
        (JNIEnv *env, jobject thiz) {
    LOGD("main mmap close!");
    munmap(mainData,26*0x1000);
}

JNIEXPORT void JNICALL Java_com_wwc2_networks_server_dvr_jni_DvrDataNative_closeSub
        (JNIEnv *env, jobject thiz) {
    LOGD("sub mmap close!");
    munmap(subData,26*0x1000);
}

#define DEV_INFO_NODE			"/dev/devmap"
#define DEV_IOC_MAGIC	   		'd'
#define READ_DEV_DATA	   		_IOR(DEV_IOC_MAGIC,  1, unsigned int)
#define DO_VERIFY_UPDATA		_IOW(DEV_IOC_MAGIC,  2, unsigned int)
#define WRITE_VERIFY_DATA		_IOW(DEV_IOC_MAGIC,  3, unsigned int)

JNIEXPORT jint JNICALL Java_com_wwc2_networks_server_dvr_jni_DvrDataNative_getKeyO
        (JNIEnv *env, jobject obj){
    LOGD("getKeyO!");

    int dev_fd = open(DEV_INFO_NODE,O_RDWR);
    unsigned int hw_key1=12;
    ioctl(dev_fd, READ_DEV_DATA, &hw_key1);
    LOGD("hw_key1 = 0x%08x",hw_key1);

    close(dev_fd);

    return (jint)hw_key1;
}

JNIEXPORT jint JNICALL Java_com_wwc2_networks_server_dvr_jni_DvrDataNative_getKeyT
        (JNIEnv *env, jobject obj){
    LOGD("getKeyT!");

    int dev_fd = open(DEV_INFO_NODE,O_RDWR);
    unsigned int hw_key2=13;
    ioctl(dev_fd, READ_DEV_DATA, &hw_key2);
    LOGD("hw_key2 = 0x%08x",hw_key2);
    close(dev_fd);

    return (jint)hw_key2;

}

JNIEXPORT jint JNICALL Java_com_wwc2_networks_server_dvr_jni_DvrDataNative_getOsStatus
  (JNIEnv *env, jobject obj){
    LOGD("getOsStatus!");

    int dev_fd = open(DEV_INFO_NODE,O_RDWR);
    unsigned int hw_key3=19;
    ioctl(dev_fd, READ_DEV_DATA, &hw_key3);
    LOGD("hw_key3 = 0x%08x",hw_key3);
    close(dev_fd);
    if(hw_key3 == 0x50415353)
    {
        //LOGD(" true ");
        return 1;
    }else
    {
        //LOGD(" false ");
    }
    return 0;
}