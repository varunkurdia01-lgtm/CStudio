import re

path = "/app/applet/app/src/main/java/com/example/api/CompilerService.kt"
with open(path, "r") as f:
    content = f.read()

target1 = """data class CompileRequest(
    val code: String,
    val language: String = "c",
    val stdin: String = ""
)"""
rep1 = """data class CompileRequest(
    val code: String,
    val language: String = "c",
    val stdin: String = "",
    val compiler: String = "cg132"
)"""

target2 = """            val apiRequest = GodboltRequest(
                source = request.code,
                options = GodboltOptions(
                    executeParameters = GodboltExecuteParameters(
                        stdin = request.stdin
                    )
                )
            )"""
rep2 = """            val apiRequest = GodboltRequest(
                source = request.code,
                compiler = request.compiler,
                options = GodboltOptions(
                    executeParameters = GodboltExecuteParameters(
                        stdin = request.stdin
                    )
                )
            )"""

content = content.replace(target1, rep1).replace(target2, rep2)
with open(path, "w") as f:
    f.write(content)
