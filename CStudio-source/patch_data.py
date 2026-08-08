import re

with open('/app/applet/app/src/main/java/com/example/ui/screens/EditorViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace("val undoStack: MutableList<TextFieldValue> = mutableListOf()",
                          "val undoStack: MutableList<TextFieldValue> = androidx.compose.runtime.mutableStateListOf()")
content = content.replace("val redoStack: MutableList<TextFieldValue> = mutableListOf()",
                          "val redoStack: MutableList<TextFieldValue> = androidx.compose.runtime.mutableStateListOf()")

with open('/app/applet/app/src/main/java/com/example/ui/screens/EditorViewModel.kt', 'w') as f:
    f.write(content)
