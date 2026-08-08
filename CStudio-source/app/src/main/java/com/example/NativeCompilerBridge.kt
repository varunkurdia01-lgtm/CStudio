package com.example

object NativeCompilerBridge {
    init {
        try {
            System.loadLibrary("cstudio")
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }
    }

    external fun nativeTest(): String

    fun testNativeBridge(): String {
        return try {
            nativeTest()
        } catch (e: Throwable) {
            "Native bridge failed: ${e.message}"
        }
    }
}
