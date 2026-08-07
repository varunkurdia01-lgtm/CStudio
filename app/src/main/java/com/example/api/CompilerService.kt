package com.example.api

import kotlinx.serialization.Serializable
import com.example.BuildConfig

@Serializable
data class CompileRequest(
    val code: String,
    val language: String = "c",
    val stdin: String = "",
    val compiler: String = "cg132"
)

@Serializable
data class CompileResponse(
    val stdout: String,
    val stderr: String,
    val executionTimeMs: Long
)

interface CompilerService {
    suspend fun compileCode(request: CompileRequest): CompileResponse
}

class GodboltCompilerService : CompilerService {
    override suspend fun compileCode(request: CompileRequest): CompileResponse {
        val startTime = System.currentTimeMillis()
        
        return try {
            val apiRequest = GodboltRequest(
                source = request.code,
                compiler = request.compiler,
                options = GodboltOptions(
                    executeParameters = GodboltExecuteParameters(
                        stdin = request.stdin
                    )
                )
            )
            
            val response = GodboltClient.service.compileCode(apiRequest)
            val executionTimeMs = System.currentTimeMillis() - startTime
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val ansiRegex = Regex("\u001B\\[[;\\d]*[ -/]*[@-~]")
                    
                    if (body.code != 0) {
                        // Compilation failed
                        val stderrText = body.stderr.joinToString("\n") { it.text }.replace(ansiRegex, "")
                        CompileResponse(
                            stdout = "",
                            stderr = stderrText.ifEmpty { "Unknown compilation error" },
                            executionTimeMs = executionTimeMs
                        )
                    } else if (body.execResult != null) {
                        // Execution succeeded
                        val execResult = body.execResult
                        val stdoutText = execResult.stdout.joinToString("\n") { it.text }.replace(ansiRegex, "")
                        val stderrText = execResult.stderr.joinToString("\n") { it.text }.replace(ansiRegex, "")
                        CompileResponse(
                            stdout = stdoutText,
                            stderr = stderrText,
                            executionTimeMs = execResult.execTime
                        )
                    } else {
                        // No execution result, but compiled successfully
                        CompileResponse(
                            stdout = "",
                            stderr = "Execution did not run on server",
                            executionTimeMs = executionTimeMs
                        )
                    }
                } else {
                    CompileResponse(
                        stdout = "",
                        stderr = "Empty response from server",
                        executionTimeMs = executionTimeMs
                    )
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown HTTP error"
                CompileResponse(
                    stdout = "",
                    stderr = "Network Error: HTTP ${response.code()}\nDetails: $errorBody",
                    executionTimeMs = executionTimeMs
                )
            }
        } catch (e: Exception) {
            CompileResponse(
                stdout = "",
                stderr = "Network Error: ${e.message}\n${e.stackTraceToString()}\nPlease check your internet connection.",
                executionTimeMs = System.currentTimeMillis() - startTime
            )
        }
    }
}

object ServiceProvider {
    val compilerService: CompilerService by lazy {
        GodboltCompilerService()
    }
    
    fun getLocalCompilerService(context: android.content.Context): CompilerService {
        return LocalCompilerService(context)
    }
}
