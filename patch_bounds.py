import sys

with open("app/src/main/java/com/example/ui/screens/EditorViewModel.kt", "r") as f:
    content = f.read()

content = content.replace(
    "if (newValue.text.length > activeFile.content.text.length) {",
    "if (newValue.selection.start > 0 && newValue.text.length > activeFile.content.text.length) {"
)

content = content.replace(
    "if (finalValue.text.length > activeFile.content.text.length && finalValue.text.getOrNull(finalValue.selection.start - 1) == '\\n') {",
    "if (finalValue.selection.start > 0 && finalValue.text.length > activeFile.content.text.length && finalValue.text.getOrNull(finalValue.selection.start - 1) == '\\n') {"
)

content = content.replace(
    "return TextFieldValue(newText, androidx.compose.ui.text.TextRange(tfv.selection.start + text.length))",
    "return TextFieldValue(newText, androidx.compose.ui.text.TextRange(tfv.selection.start + text.length), tfv.composition)"
)

content = content.replace(
    "return TextFieldValue(newText, tfv.selection)",
    "return TextFieldValue(newText, tfv.selection, tfv.composition)"
)


with open("app/src/main/java/com/example/ui/screens/EditorViewModel.kt", "w") as f:
    f.write(content)
