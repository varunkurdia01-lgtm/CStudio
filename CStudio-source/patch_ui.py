import re

with open('/app/applet/app/src/main/java/com/example/ui/screens/EditorScreen.kt', 'r') as f:
    content = f.read()

replacement = """                        if (viewModel.isCompiling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        } else if (viewModel.isCompileSuccess == true) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Compilation Successful",
                                tint = Color.Green,
                                modifier = Modifier.size(16.dp)
                            )
                        } else if (viewModel.isCompileSuccess == false) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "Compilation Failed",
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                        }"""

content = re.sub(r'                        if \(viewModel\.isCompiling\) \{\n\s*CircularProgressIndicator\(\n\s*modifier = Modifier\.size\(16\.dp\),\n\s*strokeWidth = 2\.dp,\n\s*strokeCap = androidx\.compose\.ui\.graphics\.StrokeCap\.Round\n\s*\)\n\s*\}', replacement, content)

# ensure Icons.Default.CheckCircle and Icons.Default.Error are imported
if "import androidx.compose.material.icons.filled.CheckCircle" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.PlayArrow", "import androidx.compose.material.icons.filled.PlayArrow\nimport androidx.compose.material.icons.filled.CheckCircle\nimport androidx.compose.material.icons.filled.Error")

with open('/app/applet/app/src/main/java/com/example/ui/screens/EditorScreen.kt', 'w') as f:
    f.write(content)
