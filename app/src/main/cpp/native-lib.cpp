#include <jni.h>
#include <string>
#include "HazeRemoveFilter.h"

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_cn_wangbaiyuan_hazeremovecamera_filter_HazeRemoveFilter_processImage(JNIEnv *env,
                                                                          jobject instance,
                                                                          jobjectArray inPixelsArr,
                                                                          jint mImageWidth,
                                                                          jint mImageHeight) {


    //获取数组行数
    int **inPixels = new int *[mImageHeight];
    double **guideImage = new double *[mImageHeight];
    for (int i = 0; i < mImageHeight; i++) {
        inPixels[i] = new int[mImageWidth];
        guideImage[i] = new double[mImageWidth];
        jintArray intdata = (jintArray) env->GetObjectArrayElement(inPixelsArr, i);
        inPixels[i] = env->GetIntArrayElements(intdata, 0);
//        env->DeleteLocalRef(intdata);
        for (int j = 0; j < mImageWidth; j++) {
            guideImage[i][j] = ((inPixels[i][j] & 0x00ff0000) >> 16) * 0.3 +
                               ((inPixels[i][j] & 0x0000ff00) >> 8) * 0.59 +
                               (inPixels[i][j] & 0x000000ff) * 0.11;

        }
    }

    HazeRemoveFilter hazeRemoveFilter(inPixels, guideImage, mImageWidth, mImageHeight);
    hazeRemoveFilter.process();
    int **out = hazeRemoveFilter.outPixels;
    jobjectArray result= env->NewObjectArray(mImageHeight,env->FindClass("[I"),NULL);

    for (int i = 0; i < mImageHeight; i++) {
        jintArray jintArray= env->NewIntArray(mImageWidth);
        env->SetIntArrayRegion(jintArray, 0, mImageWidth, out[i]);
        env->SetObjectArrayElement(result, i, jintArray);
        env->DeleteLocalRef(jintArray);
    }

    return result;
}


extern "C"
jstring
Java_cn_wangbaiyuan_hazeremovecamera_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";

    return env->NewStringUTF(hello.c_str());
}
