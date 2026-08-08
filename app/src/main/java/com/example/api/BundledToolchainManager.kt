package com.example.api

import android.content.Context
import java.io.File

class BundledToolchainManager(private val context: Context) {

    private val executionRoot: File
        get() = File(context.filesDir, "cstudio_runtime")

    private val legacyToolchainDir: File
        get() = File(context.codeCacheDir, "cstudio_runtime/toolchain")

    /**
     * Active install location for the offline compiler.
     *
     * We keep the runtime in filesDir so it survives cache clearing.
     * Existing installs from the previous codeCacheDir layout are migrated
     * automatically the first time the app touches the toolchain.
     */
    val toolchainDir: File
        get() = resolveToolchainDir()

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

    fun getCCompiler(): File = File(binDir, "clang")

    /**
     * C++ compilation is driven by the same clang binary with -x c++.
     * We keep this method for compatibility with the existing codebase.
     */
    fun getCppCompiler(): File = File(binDir, "clang")

    fun getLinker(): File = File(binDir, "ld.lld")

    fun getLibcxxShared(): File = File(libDir, "libc++_shared.so")

    fun getLibLLVM(): File = File(libDir, "libLLVM.so")

    fun getLibClangCpp(): File = File(libDir, "libclang-cpp.so")

    fun getIncludes(): File = includeDir

    fun getAndroidHeadersDir(): File = File(includeDir, "aarch64-linux-android")

    fun getClangResourceDir(): File = resourceDir

    fun createWorkspaceDir(workspaceId: String): File {
        val workspaceRoot = File(context.codeCacheDir, "compiler_workspace")
        return File(workspaceRoot, workspaceId)
    }

    fun ensureToolchainReady(): Boolean {
        val activeDir = toolchainDir
        ensureExecutablePermissions(activeDir)
        return isToolchainBundled
    }

    val isToolchainBundled: Boolean
        get() {
            val activeDir = toolchainDir
            ensureExecutablePermissions(activeDir)
            return activeDir.exists() &&
                getCCompiler().isFile &&
                getLinker().isFile &&
                getLibcxxShared().isFile &&
                getLibLLVM().isFile &&
                getLibClangCpp().isFile &&
                includeDir.isDirectory &&
                getAndroidHeadersDir().isDirectory &&
                getClangResourceDir().isDirectory &&
                getCCompiler().canExecute() &&
                getLinker().canExecute()
        }

    fun checkToolchainStatus(): String {
        val activeDir = toolchainDir
        ensureExecutablePermissions(activeDir)

        val sb = StringBuilder()
        sb.append("Bundled Toolchain Status:\n")
        sb.append("Runtime Root: ${executionRoot.absolutePath}\n")
        sb.append("Toolchain Dir Exists: ${activeDir.exists()}\n")
        sb.append("Toolchain Dir: ${activeDir.absolutePath}\n")
        sb.append("Legacy Toolchain Dir Exists: ${legacyToolchainDir.exists()}\n\n")

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
            sb.append("\nLocal compiler toolchain is not installed yet.")
        } else {
            sb.append("\nToolchain is ready.")
        }

        return sb.toString()
    }

    private fun resolveToolchainDir(): File {
        val preferredDir = File(executionRoot, "toolchain")
        if (preferredDir.exists()) {
            ensureExecutablePermissions(preferredDir)
            return preferredDir
        }

        if (legacyToolchainDir.exists()) {
            preferredDir.parentFile?.mkdirs()
            val moved = legacyToolchainDir.renameTo(preferredDir)
            if (!moved) {
                legacyToolchainDir.copyRecursively(preferredDir, overwrite = true)
                legacyToolchainDir.deleteRecursively()
            }
            ensureExecutablePermissions(preferredDir)
            return preferredDir
        }

        preferredDir.parentFile?.mkdirs()
        return preferredDir
    }

    private fun ensureExecutablePermissions(dir: File) {
        if (!dir.exists()) return

        // Make sure native binaries inside any */bin/ directory are executable.
        dir.walkTopDown()
            .filter { it.isFile && it.parentFile?.name == "bin" }
            .forEach { file ->
                try {
                    file.setReadable(true, true)
                    file.setWritable(true, true)
                    file.setExecutable(true, true)
                } catch (_: SecurityException) {
                    // Best effort only; we still keep validation in isToolchainBundled.
                }
            }
    }
}
