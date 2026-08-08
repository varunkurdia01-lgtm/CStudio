import re

path = "/app/applet/app/src/main/java/com/example/CStudioApp.kt"
with open(path, "r") as f:
    content = f.read()

target = "val hasWorkspace = remember { mutableStateOf(settings.workspaceUri != null) }"
rep = """val hasWorkspace = remember {
        mutableStateOf(
            try {
                val uriStr = settings.workspaceUri
                if (uriStr != null) {
                    val uri = android.net.Uri.parse(uriStr)
                    val documentFile = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, uri)
                    if (documentFile != null && documentFile.canRead() && documentFile.canWrite()) {
                        true
                    } else {
                        settings.workspaceUri = null
                        false
                    }
                } else false
            } catch (e: Exception) {
                settings.workspaceUri = null
                false
            }
        )
    }"""

content = content.replace(target, rep)
with open(path, "w") as f:
    f.write(content)
