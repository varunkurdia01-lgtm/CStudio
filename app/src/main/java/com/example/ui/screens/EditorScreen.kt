package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.api.CompileRequest
import com.example.api.ServiceProvider
import kotlinx.coroutines.launch
import com.example.ui.theme.success
import com.example.ui.components.GlassContainer
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.KeyboardCapitalization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditorViewModel = viewModel()
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
        if (viewModel.isSearchVisible) {
            viewModel.toggleSearch()
        } else {
            safeNavigateBack()
        }
    }

    var showInputDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var programInput by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    val activeFile = viewModel.getActiveFile()
    var localContent by androidx.compose.runtime.remember(activeFile) { androidx.compose.runtime.mutableStateOf(activeFile?.content ?: androidx.compose.ui.text.input.TextFieldValue("")) }
    val codeText = localContent.text
    val fontSize = viewModel.settingsRepository.editorFontSize.sp
    
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE || event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                viewModel.flushSave()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.flushSave()
        }
    }
    
    if (showInputDialog) {
        AlertDialog(
            onDismissRequest = { showInputDialog = false },
            title = { Text("Program Input") },
            text = {
                Column {
                    Text("Enter the program input exactly as expected.\nExample: 10 20 or Hello World")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = programInput,
                        onValueChange = { programInput = it },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showInputDialog = false
                    viewModel.compileAndRun(programInput)
                }) {
                    Text("Run Program")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInputDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(activeFile?.fileName ?: "No File Open", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(activeFile?.projectName ?: "CStudio", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = safeNavigateBack) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleSearch() }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = { viewModel.undo()?.let { localContent = it } }, enabled = activeFile?.undoStack?.isNotEmpty() == true) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = { viewModel.redo()?.let { localContent = it } }, enabled = activeFile?.redoStack?.isNotEmpty() == true) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable(enabled = !viewModel.isCompiling && activeFile != null) {
                                val inputFunctions = listOf("scanf", "getchar", "fgets", "gets", "getc", "scanf_s")
                                if (inputFunctions.any { codeText.contains(it) }) {
                                    programInput = ""
                                    showInputDialog = true
                                } else {
                                    viewModel.compileAndRun()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Editor Area
            GlassContainer(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column {
                    // Editor Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        viewModel.openFiles.forEachIndexed { index, file ->
                            val isActive = index == viewModel.activeFileIndex
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isActive) MaterialTheme.colorScheme.surface else Color.Transparent)
                                    .clickable { viewModel.openFile(file.projectName, file.fileName) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(file.fileName, color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        Icons.Default.Close, 
                                        contentDescription = "Close", 
                                        modifier = Modifier.size(14.dp).clickable { viewModel.closeFile(index) },
                                        tint = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    if (viewModel.isSearchVisible) {
                        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = viewModel.searchQuery,
                                    onValueChange = { viewModel.updateSearchQuery(it) },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    placeholder = { Text("Search...", fontSize = 12.sp) },
                                    singleLine = true
                                )
                                Spacer(Modifier.width(4.dp))
                                IconButton(onClick = { viewModel.findPrevious() }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.KeyboardArrowUp, "Previous")
                                }
                                IconButton(onClick = { viewModel.findNext() }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.KeyboardArrowDown, "Next")
                                }
                                IconButton(onClick = { viewModel.toggleCaseSensitive() }, modifier = Modifier.size(32.dp)) {
                                    Text("Aa", color = if (viewModel.isCaseSensitive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                }
                                IconButton(onClick = { viewModel.toggleSearch() }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Close, "Close Search")
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = viewModel.replaceQuery,
                                    onValueChange = { viewModel.updateReplaceQuery(it) },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    placeholder = { Text("Replace...", fontSize = 12.sp) },
                                    singleLine = true
                                )
                                Spacer(Modifier.width(8.dp))
                                TextButton(onClick = { viewModel.replaceCurrent() }) {
                                    Text("Replace")
                                }
                                TextButton(onClick = { viewModel.replaceAll() }) {
                                    Text("All")
                                }
                            }
                        }
                    }
                    // Code Editor Area with Line Numbers
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        // Line Numbers
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 12.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            val lineCount = codeText.count { it == '\n' } + 1
                            for (i in 1..lineCount) {
                                Text(
                                    text = i.toString(),
                                    fontSize = fontSize,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }

                        // Text Field
                        if (activeFile != null) {
                            TextField(
                                value = localContent,
                                onValueChange = { 
                                    localContent = viewModel.updateCode(localContent, it) 
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 4.dp)
                                    .let { 
                                        if (!viewModel.settingsRepository.wordWrap) it.horizontalScroll(rememberScrollState()) else it 
                                    }, // align with line numbers
                                textStyle = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = fontSize,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                visualTransformation = com.example.ui.components.CodeVisualTransformation(
                                    viewModel.searchQuery, 
                                    viewModel.isCaseSensitive, 
                                    localContent.selection,
                                    syntaxColors = when (com.example.data.SettingsRepository.getThemeFlow(context).collectAsState().value) {
                                        "Light Theme" -> com.example.ui.components.LightSyntaxColors
                                        "Dark Mode" -> com.example.ui.components.DarkSyntaxColors
                                        "AMOLED Black" -> com.example.ui.components.AmoledSyntaxColors
                                        "Midnight Blue" -> com.example.ui.components.MidnightSyntaxColors
                                        else -> if (androidx.compose.foundation.isSystemInDarkTheme()) com.example.ui.components.DarkSyntaxColors else com.example.ui.components.LightSyntaxColors
                                    }
                                ),
                                keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, keyboardType = KeyboardType.Text, capitalization = KeyboardCapitalization.None),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No file open", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Output Console Area (Terminal style)
            GlassContainer(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column {
                    // Terminal Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Output",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(24.dp))
                        Text(
                            text = "Errors",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(24.dp))
                        Text(
                            text = "Console",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (viewModel.isCompiling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        } else if (viewModel.isCompileSuccess == true) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Compilation Successful",
                                tint = MaterialTheme.colorScheme.success,
                                modifier = Modifier.size(16.dp)
                            )
                        } else if (viewModel.isCompileSuccess == false) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "Compilation Failed",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                    // Terminal Output
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        item {
                            Text(
                                text = viewModel.output,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                color = if (viewModel.output.contains("error", ignoreCase = true) && viewModel.executionErrorDetails == null) com.example.ui.theme.ErrorRed else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (viewModel.executionErrorDetails != null) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                var isDetailsExpanded by remember { mutableStateOf(false) }
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isDetailsExpanded = !isDetailsExpanded }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isDetailsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Toggle Technical Details",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Technical Details",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                if (isDetailsExpanded) {
                                    Text(
                                        text = viewModel.executionErrorDetails ?: "",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                            .padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
