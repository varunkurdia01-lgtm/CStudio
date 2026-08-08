import com.example.api.*
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    try {
        val req = WandboxRequest(compiler = "gcc-head", code = "int main(){}", save = false)
        val res = WandboxClient.service.compileCode(req)
        println("Response: ${res.code()} - ${res.errorBody()?.string()}")
    } catch(e: Exception) {
        println("Exception: ${e.message}")
    }
}
