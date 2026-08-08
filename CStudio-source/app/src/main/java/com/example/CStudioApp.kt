package com.example

import kotlinx.coroutines.launch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.RowScope
import androidx.compose.ui.draw.blur
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassContainer
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.ui.screens.WorkspaceSetupScreen
import com.example.data.SettingsRepository
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.AiTutorScreen
import com.example.ui.screens.LessonsScreen

import com.example.ui.screens.FilesScreen
import com.example.ui.screens.SettingsScreen

import androidx.compose.foundation.layout.consumeWindowInsets

@Composable
fun CStudioApp() {
    val navController = rememberNavController()
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val context = LocalContext.current
    val settings = remember { SettingsRepository(context) }
    val hasWorkspace = remember {
        mutableStateOf(
            try {
                val uriStr = settings.workspaceUri
                if (uriStr != null) {
                    val uri = android.net.Uri.parse(uriStr)
                    val documentFile = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, uri)
                    if (documentFile != null && documentFile.canRead() && documentFile.canWrite()) {
                        true
                    } else {
                        settings.workspaceUri = null
                        false
                    }
                } else false
            } catch (e: Exception) {
                settings.workspaceUri = null
                false
            }
        )
    }
    val editorViewModel: com.example.ui.screens.EditorViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    if (!hasWorkspace.value) {
        WorkspaceSetupScreen(onWorkspaceSelected = { hasWorkspace.value = true })
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            if (currentRoute in listOf("home", "files", "lessons", "settings")) {
                Box(modifier = Modifier.padding(bottom = 16.dp, start = 24.dp, end = 24.dp).fillMaxWidth()) {
                    GlassContainer(
                        shape = RoundedCornerShape(24.dp),
                        elevation = 8.dp,
                        modifier = Modifier.fillMaxWidth().height(72.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NavigationBarItem(
                                icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                                label = { Text("Home") },
                                selected = currentRoute == "home",
                                onClick = { navController.navigate("home") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = Color.Transparent
                                )
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Outlined.Folder, contentDescription = "Files") },
                                label = { Text("Files") },
                                selected = currentRoute == "files",
                                onClick = { navController.navigate("files") },
                                colors = NavigationBarItemDefaults.colors(
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = Color.Transparent
                                )
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Outlined.Code, contentDescription = "Editor") },
                                label = { },
                                selected = false,
                                onClick = { navController.navigate("editor") },
                                colors = NavigationBarItemDefaults.colors(
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = Color.Transparent
                                )
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.AutoMirrored.Outlined.LibraryBooks, contentDescription = "Examples") },
                                label = { Text("Examples") },
                                selected = currentRoute == "lessons",
                                onClick = { navController.navigate("lessons") },
                                colors = NavigationBarItemDefaults.colors(
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = Color.Transparent
                                )
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") },
                                label = { Text("Settings") },
                                selected = currentRoute == "settings",
                                onClick = { navController.navigate("settings") },
                                colors = NavigationBarItemDefaults.colors(
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(250)) + scaleIn(initialScale = 0.96f, animationSpec = tween(250)) },
            exitTransition = { fadeOut(animationSpec = tween(250)) + scaleOut(targetScale = 1.04f, animationSpec = tween(250)) },
            popEnterTransition = { fadeIn(animationSpec = tween(250)) + scaleIn(initialScale = 1.04f, animationSpec = tween(250)) },
            popExitTransition = { fadeOut(animationSpec = tween(250)) + scaleOut(targetScale = 0.96f, animationSpec = tween(250)) }
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToEditor = { navController.navigate("editor") },
                    onNavigateToAiTutor = { navController.navigate("ai_tutor") },
                    onNavigateToLessons = { navController.navigate("lessons") },
                    onNavigateToFiles = { navController.navigate("files") },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onOpenFile = { proj, file -> editorViewModel.openFile(proj, file); navController.navigate("editor") }
                )
            }
            composable("editor") {
                EditorScreen(onNavigateBack = { navController.popBackStack() }, viewModel = editorViewModel)
            }
            composable("ai_tutor") {
                AiTutorScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable("lessons") {
                LessonsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onStartLesson = { lessonName ->
                        val safeName = lessonName.replace(" ", "_")
                        val repo = editorViewModel.projectRepository
                        if (repo.getProjects().none { it.name == safeName }) {
                            
                            scope.launch { repo.createFile(safeName, "main.c", "// $lessonName\n#include <stdio.h>\n\nint main() {\n    return 0;\n}") }
                        }
                        editorViewModel.openFile(safeName, "main.c")
                        navController.navigate("editor")
                    }
                )
            }
            composable("files") {
                FilesScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onOpenFile = { proj, file -> editorViewModel.openFile(proj, file); navController.navigate("editor") },
                    onFileDeleted = { proj, file -> editorViewModel.onFileDeleted(proj, file) },
                    onProjectDeleted = { proj -> editorViewModel.onProjectDeleted(proj) },
                    onProjectRenamed = { oldProj, newProj -> editorViewModel.onProjectRenamed(oldProj, newProj) },
                    onFileRenamed = { proj, oldFile, newFile -> editorViewModel.onFileRenamed(proj, oldFile, newFile) }
                )
            }
            composable("settings") {
                SettingsScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
}
