//
// Created by mahmoodms on 4/3/2017.
//

#include "rt_nonfinite.h"
#include "classifySSVEP.h"
#include "extractPowerSpectrum.h"
#include "ssvep_filter_f32.h"

/*Additional Includes*/
#include <jni.h>
#include <android/log.h>

#define  LOG_TAG "jniExecutor-cpp"
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Function Definitions
extern "C" {
JNIEXPORT jfloatArray JNICALL
Java_com_yeolabgt_mahmoodms_androidblekotlin_DeviceControlActivity_jSSVEPCfilter(
        JNIEnv *env, jobject jobject1, jdoubleArray data) {
    jdouble *X1 = env->GetDoubleArrayElements(data, NULL);
    float Y[1000]; // First two values = Y; last 499 = cPSD
    if (X1 == NULL) LOGE("ERROR - C_ARRAY IS NULL");
    jfloatArray m_result = env->NewFloatArray(1000);
    ssvep_filter_f32(X1, Y);
    env->SetFloatArrayRegion(m_result, 0, 1000, Y);
    return m_result;
}
}

extern "C" {
JNIEXPORT jdoubleArray JNICALL
Java_com_yeolabgt_mahmoodms_androidblekotlin_DeviceControlActivity_jClassifySSVEP(
        JNIEnv *env, jobject jobject1, jdoubleArray ch1, jdoubleArray ch2, jdouble threshold) {

    jdouble *X1 = env->GetDoubleArrayElements(ch1, NULL);
    jdouble *X2 = env->GetDoubleArrayElements(ch2, NULL);
    double Y[501]; // First two values = Y; last 499 = cPSD
    if (X1 == NULL) LOGE("ERROR - C_ARRAY IS NULL");
    if (X2 == NULL) LOGE("ERROR - C_ARRAY IS NULL");
    jdoubleArray m_result = env->NewDoubleArray(501);
    classifySSVEP(X1, X2, threshold, &Y[0], &Y[2]);
    env->SetDoubleArrayRegion(m_result, 0, 501, Y);
    return m_result;
}
}

extern "C" {
JNIEXPORT jdoubleArray JNICALL
Java_com_yeolabgt_mahmoodms_androidblekotlin_DeviceControlActivity_jPSDExtraction(
        JNIEnv *env, jobject jobject1, jdoubleArray ch1, jdoubleArray ch2, jint sampleRate, jint length) {
    jdouble *X1 = env->GetDoubleArrayElements(ch1, NULL); if (X1 == NULL) LOGE("ERROR - C_ARRAY IS NULL");
    jdouble *X2 = env->GetDoubleArrayElements(ch2, NULL); if (X2 == NULL) LOGE("ERROR - C_ARRAY IS NULL");
    if(length==0) {
        LOGE("ERROR: LENGTH INVALID");
        return nullptr;
    } else {
        jdoubleArray m_result = env->NewDoubleArray(length);
        double Y[length]; //length/2*2=Divide by two for normal length, but we are looking at 2 vectors.
        int PSD_size[2];
        extractPowerSpectrum(X1, &length, X2, &length, sampleRate, &Y[0], PSD_size);
        env->SetDoubleArrayRegion(m_result, 0, length, Y);
        return m_result;
    }
}
}

extern "C" {
JNIEXPORT jdoubleArray JNICALL
/**
 *
 * @param env
 * @param jobject1
 * @return array of frequencies (Hz) corresponding to a raw input signal.
 */
Java_com_yeolabgt_mahmoodms_androidblekotlin_DeviceControlActivity_jLoadfPSD(
        JNIEnv *env, jobject jobject1, jint sampleRate) {
    jdoubleArray m_result = env->NewDoubleArray(sampleRate);
    double fPSD[sampleRate];
    for (int i = 0; i < sampleRate; i++) {
        fPSD[i] = (double)i * (double)sampleRate / (double) (sampleRate*2);
    }
    env->SetDoubleArrayRegion(m_result, 0, sampleRate, fPSD);
    return m_result;
}
}

extern "C" {
JNIEXPORT jint JNICALL
Java_com_yeolabgt_mahmoodms_androidblekotlin_DeviceControlActivity_jmainInitialization(
        JNIEnv *env, jobject obj, jboolean terminate) {
    if (!(bool) terminate) {
        classifySSVEP_initialize();
        extractPowerSpectrum_initialize();
        return 0;
    } else {
        return -1;
    }
}
}
