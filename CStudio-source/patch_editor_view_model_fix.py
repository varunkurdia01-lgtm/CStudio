import sys

with open("app/src/main/java/com/example/ui/screens/EditorViewModel.kt", "r") as f:
    content = f.read()

# I will just replace `getActiveFile() ?: return newValue` with `getActiveFile() ?: return` globally,
# and then replace ONLY the one in `updateCode` to `getActiveFile() ?: return newValue`.

content = content.replace("getActiveFile() ?: return newValue", "getActiveFile() ?: return")

# Now specifically inside updateCode:
content = content.replace(
    "fun updateCode(newValue: TextFieldValue): TextFieldValue {\n        val activeFile = getActiveFile() ?: return",
    "fun updateCode(newValue: TextFieldValue): TextFieldValue {\n        val activeFile = getActiveFile() ?: return newValue"
)

with open("app/src/main/java/com/example/ui/screens/EditorViewModel.kt", "w") as f:
    f.write(content)
