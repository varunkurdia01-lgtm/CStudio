package com.example.ui.screens
import androidx.compose.runtime.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassContainer

import com.example.data.ProjectRepository
import com.example.data.SettingsRepository
import java.text.SimpleDateFormat
import java.util.*
import com.example.data.WorkspaceFile
import com.example.ui.theme.*
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun HomeScreen(
    onNavigateToEditor: () -> Unit,
    onNavigateToAiTutor: () -> Unit,
    onNavigateToLessons: () -> Unit,
    onNavigateToFiles: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onOpenFile: (String, String) -> Unit = { _, _ -> }
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    val projectRepository = remember { ProjectRepository(context) }
    val settingsRepository = remember { SettingsRepository(context) }
    
    var lastOpenedFile by remember { mutableStateOf<Pair<String, String>?>(null) }
    var allFiles by remember { mutableStateOf<List<WorkspaceFile>>(emptyList()) }
    
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                val tabs = settingsRepository.getOpenTabsList()
                if (tabs.isNotEmpty()) {
                    val activeIdx = settingsRepository.activeTabIndex
                    if (activeIdx in tabs.indices) {
                        lastOpenedFile = tabs[activeIdx].projectName to tabs[activeIdx].fileName
                    } else {
                        lastOpenedFile = tabs.first().projectName to tabs.first().fileName
                    }
                } else {
                    lastOpenedFile = null
                }
                
                val projects = projectRepository.getProjects()
                val filesList = mutableListOf<WorkspaceFile>()
                projects.forEach { proj ->
                    filesList.addAll(projectRepository.getProjectFiles(proj.name))
                }
                allFiles = filesList.sortedByDescending { it.lastModified }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("C Studio", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 22.sp, letterSpacing = (-0.5).sp)
            }
        }

        // Main Viewport
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progress Dashboard -> Hero GlassContainer
            GlassContainer(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text("Hello, Developer! \uD83D\uDC4B", color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Let's build something amazing today.", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    GlassContainer(
                        shape = RoundedCornerShape(16.dp),
                        elevation = 4.dp,
                        onClick = { lastOpenedFile?.let { onOpenFile(it.first, it.second) } ?: onNavigateToEditor() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Code, contentDescription = "Code", tint = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Continue Coding", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(lastOpenedFile?.second ?: "main.c", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // AI Assistant Card
            GlassContainer(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                onClick = onNavigateToAiTutor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Lightbulb, contentDescription = "AI Tutor", tint = MaterialTheme.colorScheme.secondary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("AI Tutor", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Ask questions & get help", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Quick Actions Header & Grid
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Quick Actions", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickActionItem(icon = Icons.AutoMirrored.Outlined.InsertDriveFile, label = "New File", tint = MaterialTheme.colorScheme.onSurface, onClick = onNavigateToEditor)
                    QuickActionItem(icon = Icons.Outlined.Folder, label = "Open File", tint = MaterialTheme.colorScheme.onSurface, onClick = onNavigateToFiles)
                    QuickActionItem(icon = Icons.AutoMirrored.Outlined.LibraryBooks, label = "Examples", tint = MaterialTheme.colorScheme.onSurface, onClick = onNavigateToLessons)
                    QuickActionItem(icon = Icons.Outlined.Settings, label = "Settings", tint = MaterialTheme.colorScheme.onSurface, onClick = onNavigateToSettings)
                }
            }

            // Recent Files List
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Files", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text("View All", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigateToFiles() })
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                GlassContainer(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column {
                        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                        val displayFiles = allFiles.take(5)
                        if (displayFiles.isEmpty()) {
                            Text("No recent files", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            displayFiles.forEachIndexed { index, file ->
                                RecentFileItem(file.name, file.projectName, sdf.format(Date(file.lastModified)), onClick = { onOpenFile(file.projectName, file.name) })
                                if (index < displayFiles.size - 1) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun QuickActionItem(icon: ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GlassContainer(
            shape = RoundedCornerShape(16.dp),
            elevation = 4.dp,
            onClick = onClick
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun RecentFileItem(name: String, path: String, time: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (name.endsWith(".c")) "C" else "TXT",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Text(path, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
        }
        Text(time, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
}