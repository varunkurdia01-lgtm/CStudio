import sys

with open("app/src/main/java/com/example/ui/screens/EditorScreen.kt", "r") as f:
    content = f.read()

if "import androidx.compose.foundation.text.KeyboardOptions" not in content:
    content = content.replace(
        "import androidx.compose.ui.platform.LocalContext",
        "import androidx.compose.ui.platform.LocalContext\nimport androidx.compose.foundation.text.KeyboardOptions\nimport androidx.compose.ui.text.input.KeyboardType\nimport androidx.compose.ui.text.input.KeyboardCapitalization"
    )

content = content.replace(
    "visualTransformation = com.example.ui.components.CodeVisualTransformation(viewModel.searchQuery),",
    "visualTransformation = com.example.ui.components.CodeVisualTransformation(viewModel.searchQuery),\n                                keyboardOptions = KeyboardOptions(autoCorrect = false, keyboardType = KeyboardType.Text, capitalization = KeyboardCapitalization.None),"
)

with open("app/src/main/java/com/example/ui/screens/EditorScreen.kt", "w") as f:
    f.write(content)
