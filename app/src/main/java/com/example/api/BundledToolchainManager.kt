package com.example.api

import android.content.Context
import java.io.File

class BundledToolchainManager(private val context: Context) {

    // Expected install location inside app private storage.
    val toolchainDir: File
        get() = File(context.filesDir, "toolchain")

    val binDir: File
        get() = File(toolchainDir, "bin")

    val libDir: File
        get() = File(toolchainDir, "lib")

    val includeDir: File
        get() = File(toolchainDir, "include")

    val resourceDir: File
        get() = File(libDir, "clang/21")

    val resourceIncludeDir: File
        get() = File(resourceDir, "include")

    fun getCCompiler(): File {
        return File(binDir, "clang")
    }

    /**
     * C++ compilation is driven by the same clang binary with -x c++.
     * We keep this method for compatibility with the existing codebase.
     */
    fun getCppCompiler(): File {
        return File(binDir, "clang")
    }

    fun getLinker(): File {
        return File(binDir, "ld.lld")
    }

    fun getLibcxxShared(): File {
        return File(libDir, "libc++_shared.so")
    }

    fun getLibLLVM(): File {
        return File(libDir, "libLLVM.so")
    }

    fun getLibClangCpp(): File {
        return File(libDir, "libclang-cpp.so")
    }

    fun getIncludes(): File {
        return includeDir
    }

    fun getAndroidHeadersDir(): File {
        return File(includeDir, "aarch64-linux-android")
    }

    fun getClangResourceDir(): File {
        return resourceDir
    }

    val isToolchainBundled: Boolean
        get() = toolchainDir.exists() &&
            getCCompiler().exists() &&
            getLinker().exists() &&
            getLibcxxShared().exists() &&
            getLibLLVM().exists() &&
            getLibClangCpp().exists() &&
            includeDir.exists() &&
            getAndroidHeadersDir().exists() &&
            getClangResourceDir().exists()

    fun checkToolchainStatus(): String {
        val sb = StringBuilder()
        sb.append("Bundled Toolchain Status:\n")
        sb.append("Toolchain Dir Exists: ${toolchainDir.exists()}\n")
        sb.append("Toolchain Dir: ${toolchainDir.absolutePath}\n\n")

        val cCompiler = getCCompiler()
        sb.append("C Compiler (clang) Exists: ${cCompiler.exists()} (${cCompiler.absolutePath})\n")
        if (cCompiler.exists()) {
            sb.append("C Compiler Executable: ${cCompiler.canExecute()}\n")
        }

        val cppCompiler = getCppCompiler()
        sb.append("C++ Compiler Driver (clang -x c++) Exists: ${cppCompiler.exists()} (${cppCompiler.absolutePath})\n")
        if (cppCompiler.exists()) {
            sb.append("C++ Driver Executable: ${cppCompiler.canExecute()}\n")
        }

        val linker = getLinker()
        sb.append("Linker (ld.lld) Exists: ${linker.exists()} (${linker.absolutePath})\n")
        if (linker.exists()) {
            sb.append("Linker Executable: ${linker.canExecute()}\n")
        }

        val libcxx = getLibcxxShared()
        sb.append("libc++ Shared Exists: ${libcxx.exists()} (${libcxx.absolutePath})\n")

        val libLLVM = getLibLLVM()
        sb.append("libLLVM Exists: ${libLLVM.exists()} (${libLLVM.absolutePath})\n")

        val libClangCpp = getLibClangCpp()
        sb.append("libclang-cpp Exists: ${libClangCpp.exists()} (${libClangCpp.absolutePath})\n")

        val includes = getIncludes()
        sb.append("Includes Exists: ${includes.exists()} (${includes.absolutePath})\n")

        val androidHeaders = getAndroidHeadersDir()
        sb.append("Android Headers Exists: ${androidHeaders.exists()} (${androidHeaders.absolutePath})\n")

        val resourceDir = getClangResourceDir()
        sb.append("Clang Resource Dir Exists: ${resourceDir.exists()} (${resourceDir.absolutePath})\n")

        sb.append("\nArchitecture Supported: arm64-v8a (Expected)\n")

        if (!isToolchainBundled) {
            sb.append("\nLocal compiler toolchain is not bundled yet.")
        } else {
            sb.append("\nToolchain is ready.")
        }

        return sb.toString()
    }
}
