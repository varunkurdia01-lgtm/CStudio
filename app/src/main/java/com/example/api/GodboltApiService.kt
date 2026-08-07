package com.example.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

@Serializable
data class GodboltExecuteParameters(
    val args: String = "",
    val stdin: String = ""
)

@Serializable
data class GodboltCompilerOptions(
    val produceAst: Boolean = false,
    val produceIr: Boolean = false,
    val produceOptInfo: Boolean = false,
    val produceGccDump: Map<String, String> = emptyMap()
)

@Serializable
data class GodboltFilters(
    val execute: Boolean = true
)

@Serializable
data class GodboltOptions(
    val userArguments: String = "",
    val executeParameters: GodboltExecuteParameters = GodboltExecuteParameters(),
    val compilerOptions: GodboltCompilerOptions = GodboltCompilerOptions(),
    val filters: GodboltFilters = GodboltFilters()
)

@Serializable
data class GodboltRequest(
    val source: String,
    val compiler: String = "cg132",
    val options: GodboltOptions = GodboltOptions()
)

@Serializable
data class GodboltText(
    val text: String
)

@Serializable
data class GodboltExecResult(
    val code: Int = 0,
    val stdout: List<GodboltText> = emptyList(),
    val stderr: List<GodboltText> = emptyList(),
    val execTime: Long = 0
)

@Serializable
data class GodboltResponse(
    val code: Int = 0,
    val stdout: List<GodboltText> = emptyList(),
    val stderr: List<GodboltText> = emptyList(),
    val execResult: GodboltExecResult? = null
)

interface GodboltApiService {
    @Headers("Accept: application/json")
    @POST("api/compiler/cg132/compile")
    suspend fun compileCode(
        @Body request: GodboltRequest
    ): retrofit2.Response<GodboltResponse>
}

object GodboltClient {
    private const val BASE_URL = "https://godbolt.org/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val service: GodboltApiService by lazy {
        val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true; encodeDefaults = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        retrofit.create(GodboltApiService::class.java)
    }
}
