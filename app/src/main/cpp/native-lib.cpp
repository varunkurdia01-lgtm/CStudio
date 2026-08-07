#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_NativeCompilerBridge_nativeTest(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Native C++ bridge works!";
    return env->NewStringUTF(hello.c_str());
}
