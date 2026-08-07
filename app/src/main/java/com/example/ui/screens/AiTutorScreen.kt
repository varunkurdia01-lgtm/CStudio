package com.example.ui.screens
import androidx.compose.ui.draw.shadow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.activity.compose.BackHandler
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.Part
import com.example.api.RetrofitClient
import com.example.BuildConfig
import kotlinx.coroutines.launch

class AiTutorViewModel : ViewModel() {
    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> = _messages

    var isLoading by mutableStateOf(false)
        private set

    var selectedModel by mutableStateOf("gemini-1.5-flash")

    init {
        _messages.add(ChatMessage("Hello! I am your AI C Tutor. How can I help you today?", false, "model"))
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        _messages.add(ChatMessage(text, true, "user"))
        isLoading = true

        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    _messages.add(ChatMessage("Error: API Key is missing. Please configure it in the Secrets panel.", false, "model"))
                    isLoading = false
                    return@launch
                }

                val history = _messages.filter { it.role != null }.map { 
                    Content(role = it.role, parts = listOf(Part(text = it.text)))
                }

                val systemInstruction = Content(
                    parts = listOf(Part(text = "You are an expert C programming tutor for a mobile IDE called C Studio. Help the user learn C."))
                )

                val request = GenerateContentRequest(
                    contents = history,
                    systemInstruction = systemInstruction
                )

                val response = RetrofitClient.service.generateContent(selectedModel, apiKey, request)
                val responseText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "I couldn't generate a response."
                
                _messages.add(ChatMessage(responseText, false, "model"))
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _messages.add(ChatMessage("Error: HTTP ${e.code()}\n$errorBody", false, null))
            } catch (e: Exception) {
                _messages.add(ChatMessage("Error: ${e.message}", false, null))
            } finally {
                isLoading = false
            }
        }
    }
}

data class ChatMessage(val text: String, val isUser: Boolean, val role: String?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiTutorScreen(
    onNavigateBack: () -> Unit,
    viewModel: AiTutorViewModel = viewModel()
) {
    var inputText by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

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

    LaunchedEffect(viewModel.messages.size, viewModel.isLoading) {
        if (viewModel.messages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.messages.size - 1)
        }
    }

    val modelDisplayNames = mapOf(
        "gemini-1.5-flash" to "Gemini Flash (General)",
        "gemini-1.5-pro" to "Gemini Pro (Complex)",
        "gemini-2.0-flash-exp" to "Gemini 2.0 Flash (Fast)"
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("AI Tutor", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = modelDisplayNames[viewModel.selectedModel] ?: viewModel.selectedModel, 
                            style = MaterialTheme.typography.bodySmall, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = safeNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        TextButton(onClick = { menuExpanded = true }) {
                            Text(modelDisplayNames[viewModel.selectedModel]?.split(" ")?.firstOrNull() ?: "Model")
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Model Selection")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Gemini Flash (General)") },
                                onClick = { viewModel.selectedModel = "gemini-1.5-flash"; menuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Gemini Pro (Complex)") },
                                onClick = { viewModel.selectedModel = "gemini-1.5-pro"; menuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Gemini 2.0 Flash (Fast)") },
                                onClick = { viewModel.selectedModel = "gemini-2.0-flash-exp"; menuExpanded = false }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viewModel.messages) { message ->
                    ChatBubble(message)
                }
                if (viewModel.isLoading) {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(28.dp)
                                .align(Alignment.CenterHorizontally),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                            strokeWidth = 3.dp
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask a question about C...") },
                    shape = RoundedCornerShape(20.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(50))
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser
    val backgroundColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart

    val bubbleShape = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = if (isUser) 20.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 20.dp
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation = if (isUser) 4.dp else 1.dp,
                    shape = bubbleShape,
                    spotColor = Color.Black.copy(alpha = 0.05f),
                    ambientColor = Color.Black.copy(alpha = 0.02f)
                )
                .clip(bubbleShape)
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
