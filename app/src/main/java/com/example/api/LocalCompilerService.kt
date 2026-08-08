package com.example.api

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class LocalCompilerService(private val context: Context) : CompilerService {

    private val toolchainManager = BundledToolchainManager(context)

    override suspend fun compileCode(request: CompileRequest): CompileResponse = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        if (!toolchainManager.ensureToolchainReady()) {
            return@withContext CompileResponse(
                stdout = "",
                stderr = "Local compiler toolchain is not installed yet.\n\n${toolchainManager.checkToolchainStatus()}",
                executionTimeMs = System.currentTimeMillis() - startTime
            )
        }

        val workspaceId = UUID.randomUUID().toString()
        val workspaceDir = toolchainManager.createWorkspaceDir(workspaceId)
        if (!workspaceDir.exists()) {
            workspaceDir.mkdirs()
        }

        try {
            val isCpp = request.language.lowercase() in listOf("cpp", "cc", "cxx", "c++")
            val sourceExt = if (isCpp) "cpp" else "c"
            val sourceFile = File(workspaceDir, "source.$sourceExt")
            val outputFile = File(workspaceDir, "output")

            sourceFile.writeText(request.code)

            val compilerBinary = toolchainManager.getCCompiler()
            if (!compilerBinary.exists() || !compilerBinary.canExecute()) {
                return@withContext CompileResponse(
                    stdout = "",
                    stderr = "Compiler binary is present but not executable.\n\n${toolchainManager.checkToolchainStatus()}",
                    executionTimeMs = System.currentTimeMillis() - startTime
                )
            }

            val compileArgs = buildCompileArgs(
                compilerBinary = compilerBinary,
                isCpp = isCpp,
                sourceFile = sourceFile,
                outputFile = outputFile
            )

            val compileProcess = ProcessBuilder(compileArgs)
                .directory(workspaceDir)
                .also { prepareEnvironment(it) }
                .redirectErrorStream(true)
                .start()

            val compileOutput = compileProcess.inputStream.bufferedReader().readText()
            val compileExitCode = compileProcess.waitFor()

            if (compileExitCode != 0) {
                return@withContext CompileResponse(
                    stdout = "",
                    stderr = "Compilation failed (exit code $compileExitCode):\n$compileOutput",
                    executionTimeMs = System.currentTimeMillis() - startTime
                )
            }

            if (!outputFile.exists() || !outputFile.canExecute()) {
                outputFile.setExecutable(true, true)
            }

            val runProcess = ProcessBuilder(outputFile.absolutePath)
                .directory(workspaceDir)
                .also { prepareRuntimeEnvironment(it) }
                .start()

            if (request.stdin.isNotEmpty()) {
                runProcess.outputStream.bufferedWriter().use { writer ->
                    writer.write(request.stdin)
                    writer.flush()
                }
            } else {
                runProcess.outputStream.close()
            }

            val runStdout = runProcess.inputStream.bufferedReader().readText()
            val runStderr = runProcess.errorStream.bufferedReader().readText()

            val runExitCode = runProcess.waitFor()

            var combinedStderr = runStderr
            if (runExitCode != 0) {
                combinedStderr += "\nProgram exited with code $runExitCode"
            }

            if (compileOutput.isNotBlank()) {
                combinedStderr = "Compiler Output:\n$compileOutput\n" + combinedStderr
            }

            return@withContext CompileResponse(
                stdout = runStdout,
                stderr = combinedStderr.trimEnd(),
                executionTimeMs = System.currentTimeMillis() - startTime
            )

        } catch (e: Exception) {
            return@withContext CompileResponse(
                stdout = "",
                stderr = "Local Compilation Error: ${e.message}\n${e.stackTraceToString()}",
                executionTimeMs = System.currentTimeMillis() - startTime
            )
        } finally {
            workspaceDir.deleteRecursively()
        }
    }

    private fun buildCompileArgs(
        compilerBinary: File,
        isCpp: Boolean,
        sourceFile: File,
        outputFile: File
    ): List<String> {
        val args = mutableListOf<String>()

        args += compilerBinary.absolutePath
        args += "-target"
        args += "aarch64-linux-android24"
        args += "-resource-dir"
        args += toolchainManager.getClangResourceDir().absolutePath
        args += "-B"
        args += toolchainManager.binDir.absolutePath
        args += "-fuse-ld=lld"
        args += "-L"
        args += toolchainManager.libDir.absolutePath
        args += "-Wno-nullability-completeness"

        if (isCpp) {
            args += "-x"
            args += "c++"
            args += "-std=c++17"
            args += "-nostdinc"
            args += "-nostdinc++"
            args += "-isystem"
            args += toolchainManager.includeDir.resolve("c++/v1").absolutePath
            args += "-isystem"
            args += toolchainManager.includeDir.absolutePath
            args += "-isystem"
            args += toolchainManager.getAndroidHeadersDir().absolutePath
            args += "-isystem"
            args += toolchainManager.resourceIncludeDir.absolutePath
            args += "-stdlib=libc++"
            args += "-lc++_shared"
        } else {
            args += "-std=c11"
            args += "-nostdinc"
            args += "-isystem"
            args += toolchainManager.includeDir.absolutePath
            args += "-isystem"
            args += toolchainManager.getAndroidHeadersDir().absolutePath
            args += "-isystem"
            args += toolchainManager.resourceIncludeDir.absolutePath
        }

        args += sourceFile.absolutePath
        args += "-o"
        args += outputFile.absolutePath
        return args
    }

    private fun prepareEnvironment(processBuilder: ProcessBuilder) {
        val environment = processBuilder.environment()
        val libraryPath = toolchainManager.libDir.absolutePath
        environment["LD_LIBRARY_PATH"] = libraryPath
        environment["PATH"] = toolchainManager.binDir.absolutePath + File.pathSeparator + (environment["PATH"] ?: "")
    }

    private fun prepareRuntimeEnvironment(processBuilder: ProcessBuilder) {
        val environment = processBuilder.environment()
        val libraryPath = toolchainManager.libDir.absolutePath
        environment["LD_LIBRARY_PATH"] = libraryPath
        environment["PATH"] = toolchainManager.binDir.absolutePath + File.pathSeparator + (environment["PATH"] ?: "")
    }
}
