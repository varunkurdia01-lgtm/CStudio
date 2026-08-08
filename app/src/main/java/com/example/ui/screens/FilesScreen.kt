package com.example.ui.screens

import kotlinx.coroutines.launch

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.activity.compose.BackHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProjectRepository
import com.example.ui.components.GlassContainer
import java.text.SimpleDateFormat
import java.util.*
import com.example.data.WorkspaceProject
import com.example.data.WorkspaceFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    onNavigateBack: () -> Unit,
    onOpenFile: (String, String) -> Unit = { _, _ -> },
    onFileDeleted: (String, String) -> Unit = { _, _ -> },
    onProjectDeleted: (String) -> Unit = { _ -> },
    onProjectRenamed: (String, String) -> Unit = { _, _ -> },
    onFileRenamed: (String, String, String) -> Unit = { _, _, _ -> }
) {
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
    val repository = remember { ProjectRepository(context) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var projects by remember { mutableStateOf(repository.getProjects()) }
    var expandedProject by remember { mutableStateOf<String?>(null) }
    
    var searchQuery by remember { mutableStateOf("") }
    val filteredProjects = remember(projects, searchQuery) {
        if (searchQuery.isBlank()) {
            projects
        } else {
            projects.filter { project ->
                project.name.contains(searchQuery, ignoreCase = true) ||
                repository.getProjectFiles(project.name).any { it.name.contains(searchQuery, ignoreCase = true) }
            }
        }
    }
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var newProjectName by remember { mutableStateOf("") }
    var starterLanguage by remember { mutableStateOf("C") }
    
    var showRenameDialog by remember { mutableStateOf<String?>(null) }
    var renameProjectName by remember { mutableStateOf("") }
    
    var showRenameFileDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    var renameFileName by remember { mutableStateOf("") }

    if (showRenameFileDialog != null) {
        val proj = showRenameFileDialog!!.first
        val file = showRenameFileDialog!!.second
        AlertDialog(
            onDismissRequest = { showRenameFileDialog = null },
            title = { Text("Rename File") },
            text = {
                OutlinedTextField(
                    value = renameFileName,
                    onValueChange = { renameFileName = it },
                    label = { Text("New File Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (renameFileName.isNotBlank()) {
                        scope.launch {
                            repository.renameFile(proj, file, renameFileName)
                            onFileRenamed(proj, file, renameFileName)
                            projects = repository.getProjects()
                        }
                    }
                    showRenameFileDialog = null
                    renameFileName = ""
                }) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameFileDialog = null }) { Text("Cancel") }
            }
        )
    }

    if (showRenameDialog != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Rename Project") },
            text = {
                OutlinedTextField(
                    value = renameProjectName,
                    onValueChange = { renameProjectName = it },
                    label = { Text("New Project Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (renameProjectName.isNotBlank() && showRenameDialog != null) {
                        repository.renameProject(showRenameDialog!!, renameProjectName)
                        onProjectRenamed(showRenameDialog!!, renameProjectName)
                        projects = repository.getProjects()
                    }
                    showRenameDialog = null
                    renameProjectName = ""
                }) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) { Text("Cancel") }
            }
        )
    }

if (showCreateDialog) {
    AlertDialog(
        onDismissRequest = { showCreateDialog = false },
        title = { Text("Create Project") },
        text = {
            Column {
                OutlinedTextField(
                    value = newProjectName,
                    onValueChange = { newProjectName = it },
                    label = { Text("Project Name") },
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                Text("Starter file", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = starterLanguage == "C",
                        onClick = { starterLanguage = "C" }
                    )
                    Text("C")
                    Spacer(Modifier.width(12.dp))
                    RadioButton(
                        selected = starterLanguage == "C++",
                        onClick = { starterLanguage = "C++" }
                    )
                    Text("C++")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (newProjectName.isNotBlank()) {
                    val name = newProjectName
                    val fileName = if (starterLanguage == "C++") "main.cpp" else "main.c"
                    val starterCode = if (starterLanguage == "C++") {
                        "#include <iostream>

int main() {
    std::cout << \"Hello\";
    return 0;
}"
                    } else {
                        "#include <stdio.h>

int main() {
    printf(\"Hello\");
    return 0;
}"
                    }
                    scope.launch {
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            repository.createProject(name)
                            repository.createFile(name, fileName, starterCode)
                        }
                        projects = repository.getProjects()
                    }
                }
                showCreateDialog = false
                newProjectName = ""
                starterLanguage = "C"
            }) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = {
                showCreateDialog = false
                starterLanguage = "C"
            }) { Text("Cancel") }
        }
    )
}

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Projects") },
                navigationIcon = {
                    IconButton(onClick = safeNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, "New Project")
                    }
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
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search projects or files...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear Search")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    )
                )
            }
            items(filteredProjects) { project ->
                GlassContainer(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                expandedProject = if (expandedProject == project.name) null else project.name
                            },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Folder, "Project", tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(project.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                                    Text("Modified: ${sdf.format(Date(project.lastModified))}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, "More Options")
                                }
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Rename") },
                                        onClick = {
                                            renameProjectName = project.name
                                            showRenameDialog = project.name
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Duplicate") },
                                        onClick = {
                                            scope.launch {
                                                repository.duplicateProject(project.name)
                                                projects = repository.getProjects()
                                            }
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            repository.deleteProject(project.name)
                                            onProjectDeleted(project.name)
                                            projects = repository.getProjects()
                                            showMenu = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        if (expandedProject == project.name) {
                            Spacer(Modifier.height(12.dp))
                            val files = repository.getProjectFiles(project.name)
                            files.forEach { file ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { onOpenFile(project.name, file.name) }.padding(vertical = 8.dp, horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Description, "File", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(file.name, fontSize = 14.sp)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = {
                                            renameFileName = file.name
                                            showRenameFileDialog = Pair(project.name, file.name)
                                        }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Edit, "Rename File", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        IconButton(onClick = {
                                            scope.launch {
                                                repository.deleteFile(project.name, file.name)
                                                onFileDeleted(project.name, file.name)
                                                projects = repository.getProjects() // Refresh
                                            }
                                        }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Close, "Delete File", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                            
                            var newFileName by remember { mutableStateOf("") }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                                OutlinedTextField(
                                    value = newFileName,
                                    onValueChange = { newFileName = it },
                                    placeholder = { Text("New file name (.c or .h)", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    singleLine = true
                                )
                                Spacer(Modifier.width(8.dp))
                                IconButton(onClick = {
                                    if (newFileName.isNotBlank()) {
                                        scope.launch {
                                            repository.createFile(project.name, newFileName)
                                            newFileName = ""
                                            projects = repository.getProjects()
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Add, "Add File")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

