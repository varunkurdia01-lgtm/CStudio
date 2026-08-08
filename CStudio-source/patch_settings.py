import re

path = "/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt"
with open(path, "r") as f:
    content = f.read()

# Make sure kotlinx.coroutines is imported
if "import kotlinx.coroutines.launch" not in content:
    content = content.replace("package com.example.ui.screens", "package com.example.ui.screens\n\nimport kotlinx.coroutines.launch\nimport kotlinx.coroutines.withContext\nimport kotlinx.coroutines.Dispatchers\nimport androidx.documentfile.provider.DocumentFile")

# State variables and launchers
target_state = "    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->"
replacement_state = """    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) {
            isExporting = true
            scope.launch(Dispatchers.IO) {
                try {
                    val workspaceUriStr = settings.workspaceUri
                    if (workspaceUriStr != null) {
                        val workspaceUri = Uri.parse(workspaceUriStr)
                        val workspaceDir = DocumentFile.fromTreeUri(context, workspaceUri)
                        if (workspaceDir != null) {
                            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                                java.util.zip.ZipOutputStream(outputStream).use { zipOut ->
                                    fun zipFolder(dir: DocumentFile, path: String) {
                                        dir.listFiles().forEach { file ->
                                            if (file.isDirectory) {
                                                zipOut.putNextEntry(java.util.zip.ZipEntry(path + file.name + "/"))
                                                zipOut.closeEntry()
                                                zipFolder(file, path + file.name + "/")
                                            } else if (file.isFile) {
                                                zipOut.putNextEntry(java.util.zip.ZipEntry(path + file.name))
                                                context.contentResolver.openInputStream(file.uri)?.use { input ->
                                                    input.copyTo(zipOut)
                                                }
                                                zipOut.closeEntry()
                                            }
                                        }
                                    }
                                    zipFolder(workspaceDir, "")
                                }
                            }
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Export successful", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                } finally {
                    isExporting = false
                }
            }
        }
    }
    
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            isImporting = true
            scope.launch(Dispatchers.IO) {
                try {
                    val workspaceUriStr = settings.workspaceUri
                    if (workspaceUriStr != null) {
                        val workspaceUri = Uri.parse(workspaceUriStr)
                        val workspaceDir = DocumentFile.fromTreeUri(context, workspaceUri)
                        if (workspaceDir != null) {
                            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                java.util.zip.ZipInputStream(inputStream).use { zipIn ->
                                    var entry = zipIn.nextEntry
                                    while (entry != null) {
                                        if (entry.isDirectory) {
                                            var currentDir = workspaceDir
                                            val parts = entry.name.split("/").filter { it.isNotEmpty() }
                                            for (part in parts) {
                                                val nextDir = currentDir.findFile(part)
                                                currentDir = nextDir ?: currentDir.createDirectory(part) ?: currentDir
                                            }
                                        } else {
                                            val parts = entry.name.split("/").filter { it.isNotEmpty() }
                                            var currentDir = workspaceDir
                                            for (i in 0 until parts.size - 1) {
                                                val part = parts[i]
                                                val nextDir = currentDir.findFile(part)
                                                currentDir = nextDir ?: currentDir.createDirectory(part) ?: currentDir
                                            }
                                            val fileName = parts.last()
                                            var file = currentDir.findFile(fileName)
                                            if (file == null) {
                                                val ext = if (fileName.contains(".")) fileName.substringAfterLast(".") else ""
                                                val mime = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
                                                file = currentDir.createFile(mime, fileName)
                                            }
                                            if (file != null) {
                                                context.contentResolver.openOutputStream(file.uri)?.use { out ->
                                                    zipIn.copyTo(out)
                                                }
                                            }
                                        }
                                        zipIn.closeEntry()
                                        entry = zipIn.nextEntry
                                    }
                                }
                            }
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Import successful", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                } finally {
                    isImporting = false
                }
            }
        }
    }
    
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->"""

content = content.replace(target_state, replacement_state)

target_system_card = """                        TextButton(onClick = { 
                            try {
                                Toast.makeText(context, "Workspace is accessible via File Manager. You can copy the folder to backup.", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }) {
                            Text("Export Projects")
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        TextButton(onClick = {
                            try {
                                Toast.makeText(context, "To import, copy your files into the selected Workspace folder using File Manager.", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }) {
                            Text("Import Projects")
                        }"""

replacement_system_card = """                        TextButton(onClick = { 
                            exportLauncher.launch("workspace_backup.zip")
                        }, enabled = !isExporting) {
                            Text(if (isExporting) "Exporting..." else "Export Projects")
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        TextButton(onClick = {
                            importLauncher.launch(arrayOf("application/zip"))
                        }, enabled = !isImporting) {
                            Text(if (isImporting) "Importing..." else "Import Projects")
                        }"""

content = content.replace(target_system_card, replacement_system_card)

with open(path, "w") as f:
    f.write(content)
