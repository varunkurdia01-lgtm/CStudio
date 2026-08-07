import re

path = "/app/applet/app/src/main/java/com/example/ui/screens/EditorViewModel.kt"
with open(path, "r") as f:
    content = f.read()

target = "val request = CompileRequest(code = code, stdin = stdin)"
rep = "val request = CompileRequest(code = code, stdin = stdin, compiler = settingsRepository.compiler)"

content = content.replace(target, rep)
with open(path, "w") as f:
    f.write(content)
