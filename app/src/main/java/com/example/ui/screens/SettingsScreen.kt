package com.example.ui.screens

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.documentfile.provider.DocumentFile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.activity.compose.BackHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SettingsRepository
import android.widget.Toast
import com.example.utils.ZipUtils
import com.example.api.ToolchainInstaller
import java.io.File
import com.example.ui.components.GlassContainer

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val safeNavigateBack = {
        keyboardController?.hide()
        focusManager.clearFocus()
        onNavigateBack()
    }

    BackHandler {
        safeNavigateBack()
    }
    val settings = remember { SettingsRepository(context) }
    
    var theme by remember { mutableStateOf(settings.theme) }
    var editorFontSize by remember { mutableStateOf(settings.editorFontSize) }
    var tabSize by remember { mutableStateOf(settings.tabSize) }
    var wordWrap by remember { mutableStateOf(settings.wordWrap) }
    var autoSave by remember { mutableStateOf(settings.autoSave) }
    var compiler by remember { mutableStateOf(settings.compiler) }

    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var isInstallingToolchain by remember { mutableStateOf(false) }
    
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
                                            var currentDir: DocumentFile = workspaceDir
                                            val parts = entry.name.split("/").filter { it.isNotEmpty() }
                                            for (part in parts) {
                                                val nextDir = currentDir.findFile(part)
                                                currentDir = nextDir ?: currentDir.createDirectory(part) ?: currentDir
                                            }
                                        } else {
                                            val parts = entry.name.split("/").filter { it.isNotEmpty() }
                                            var currentDir: DocumentFile = workspaceDir
                                            for (i in 0 until parts.size - 1) {
                                                val part = parts[i]
                                                val nextDir = currentDir.findFile(part)
                                                currentDir = nextDir ?: currentDir.createDirectory(part) ?: currentDir
                                            }
                                            val fileName = parts.last()
                                            var file = currentDir.findFile(fileName)
                                            if (file == null) {
                                                file = currentDir.createFile("text/plain", fileName)
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
    

    val toolchainInstallLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            isInstallingToolchain = true
            scope.launch(Dispatchers.IO) {
                try {
                    val result = ToolchainInstaller.installFromZipUri(context, uri)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                        if (result.success) {
                            compiler = "LOCAL COMPILER TEST"
                            settings.compiler = "LOCAL COMPILER TEST"
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Toolchain install failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                } finally {
                    isInstallingToolchain = false
                }
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                settings.workspaceUri = uri.toString()
                Toast.makeText(context, "Workspace updated. Please restart app.", Toast.LENGTH_LONG).show()
            }
        }
    }


    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = safeNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Appearance", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                GlassContainer(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val themes = listOf("System Default", "Light Theme", "Dark Mode", "AMOLED Black", "Midnight Blue")
                        themes.forEach { t ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = theme == t,
                                    onClick = {
                                        theme = t
                                        settings.theme = t
                                    }
                                )
                                Text(t, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            }

            item {
                Text("Editor", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                GlassContainer(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Font Size: ${editorFontSize.toInt()}sp")
                            Slider(
                                value = editorFontSize,
                                onValueChange = { editorFontSize = it; settings.editorFontSize = it },
                                valueRange = 10f..30f,
                                modifier = Modifier.width(150.dp)
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Tab Size: $tabSize")
                            Slider(
                                value = tabSize.toFloat(),
                                onValueChange = { tabSize = it.toInt(); settings.tabSize = it.toInt() },
                                valueRange = 2f..8f,
                                steps = 5,
                                modifier = Modifier.width(150.dp)
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Word Wrap")
                            Switch(checked = wordWrap, onCheckedChange = { wordWrap = it; settings.wordWrap = it })
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Auto Save")
                            Switch(checked = autoSave, onCheckedChange = { autoSave = it; settings.autoSave = it })
                        }
                    }
                }
            }
            
            item {
                Text("Build System", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                GlassContainer(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Compiler Mode", fontWeight = FontWeight.Medium)
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = compiler != "LOCAL COMPILER TEST",
                                onClick = { 
                                    compiler = "cg132"
                                    settings.compiler = "cg132"
                                }
                            )
                            Text("Godbolt Cloud (Online)")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = compiler == "LOCAL COMPILER TEST",
                                onClick = { 
                                    compiler = "LOCAL COMPILER TEST"
                                    settings.compiler = "LOCAL COMPILER TEST"
                                }
                            )
                            Text("LOCAL COMPILER TEST (Offline)")
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Current Compiler Mode: $compiler", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        TextButton(
                            onClick = { toolchainInstallLauncher.launch(arrayOf("application/zip")) },
                            enabled = !isInstallingToolchain
                        ) {
                            Text(if (isInstallingToolchain) "Installing Toolchain..." else "Install Offline Toolchain ZIP")
                        }
                        Text(
                            text = "Choose the compiler ZIP you built (bin/, lib/, include/). The app will install it into private storage.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            item {
                Text("System", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                GlassContainer(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        TextButton(onClick = { 
                            exportLauncher.launch("workspace_backup.zip")
                        }, enabled = !isExporting) {
                            Text(if (isExporting) "Exporting..." else "Export Projects")
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        TextButton(onClick = {
                            importLauncher.launch(arrayOf("application/zip"))
                        }, enabled = !isImporting) {
                            Text(if (isImporting) "Importing..." else "Import Projects")
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        TextButton(onClick = { settings.clearCache(context); Toast.makeText(context, "Cache Cleared", Toast.LENGTH_SHORT).show() }) {
                            Text("Clear Cache", color = MaterialTheme.colorScheme.error)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        TextButton(onClick = { 
                            val result = com.example.NativeCompilerBridge.testNativeBridge()
                            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                        }) {
                            Text("Test Native Bridge")
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        TextButton(onClick = { 
                            val manager = com.example.api.BundledToolchainManager(context)
                            val status = manager.checkToolchainStatus()
                            android.app.AlertDialog.Builder(context)
                                .setTitle("Toolchain Status")
                                .setMessage(status)
                                .setPositiveButton("OK", null)
                                .show()
                        }) {
                            Text("Test Local Toolchain")
                        }
                    }
                }
            }
            
            
            item {
                Text("Workspace", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                GlassContainer(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Current Path:\n" + (settings.workspaceUri ?: "Not set"),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                            launcher.launch(intent)
                        }) {
                            Text("Change Workspace Folder")
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        TextButton(onClick = {
                            val intent = Intent(Intent.ACTION_VIEW)
                            settings.workspaceUri?.let { intent.setDataAndType(Uri.parse(it), "vnd.android.document/root") }
                            context.startActivity(Intent.createChooser(intent, "Open Workspace"))
                        }) {
                            Text("Open Workspace in File Manager")
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

