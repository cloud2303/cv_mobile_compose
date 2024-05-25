#include <jni.h>
#include <string>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/highgui.hpp>
#include <android/log.h>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_cvtt_MainActivity_getHelloMessage(JNIEnv *env, jobject /* this */) {
    const char* hello = "Hello from JNI!";
    return env->NewStringUTF(hello);
}
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_cvtt_ui_components_DealImageKt_processImage(JNIEnv *env, jclass clazz, jstring inputPath, jstring outputPath) {
    const char *inputPathStr = env->GetStringUTFChars(inputPath, nullptr);
    const char *outputPathStr = env->GetStringUTFChars(outputPath, nullptr);

    if (inputPathStr == nullptr || outputPathStr == nullptr) {
        return nullptr;
    }

    try {
        // Read the image from the input path
        cv::Mat image = cv::imread(inputPathStr, cv::IMREAD_COLOR);
        if (image.empty()) {
            env->ReleaseStringUTFChars(inputPath, inputPathStr);
            env->ReleaseStringUTFChars(outputPath, outputPathStr);
            return nullptr;
        }

        // Print image size for debugging
        __android_log_print(ANDROID_LOG_DEBUG, "TAG", "image size: %d x %d", image.cols, image.rows);

        // Convert the image to grayscale
        cv::Mat grayImage;
        cv::cvtColor(image, grayImage, cv::COLOR_BGR2GRAY);

        // Save the grayscale image to the output path
        cv::imwrite(outputPathStr, grayImage);
        
        // Release the allocated memory for the C strings
        env->ReleaseStringUTFChars(inputPath, inputPathStr);
        env->ReleaseStringUTFChars(outputPath, outputPathStr);

        // Return the output path
        return env->NewStringUTF(outputPathStr);
    } catch (...) {
        env->ReleaseStringUTFChars(inputPath, inputPathStr);
        env->ReleaseStringUTFChars(outputPath, outputPathStr);
        return nullptr;
    }
}