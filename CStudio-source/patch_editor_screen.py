with open('/app/applet/app/src/main/java/com/example/ui/screens/EditorScreen.kt', 'r') as f:
    content = f.read()

old_actions = """                    IconButton(onClick = { viewModel.undo() }, enabled = activeFile?.undoStack?.isNotEmpty() == true) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = { viewModel.redo() }, enabled = activeFile?.redoStack?.isNotEmpty() == true) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo", tint = MaterialTheme.colorScheme.onSurface)
                    }"""

new_actions = """                    IconButton(onClick = { viewModel.undo()?.let { localContent = it } }, enabled = activeFile?.undoStack?.isNotEmpty() == true) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = { viewModel.redo()?.let { localContent = it } }, enabled = activeFile?.redoStack?.isNotEmpty() == true) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo", tint = MaterialTheme.colorScheme.onSurface)
                    }"""

content = content.replace(old_actions, new_actions)

with open('/app/applet/app/src/main/java/com/example/ui/screens/EditorScreen.kt', 'w') as f:
    f.write(content)
