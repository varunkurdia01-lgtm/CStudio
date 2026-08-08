import sys

with open("app/src/main/java/com/example/ui/screens/EditorScreen.kt", "r") as f:
    content = f.read()

search_ui = """                    if (viewModel.isSearchVisible) {
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
                    }"""

start_idx = content.find("                    if (viewModel.isSearchVisible) {")
end_idx = content.find("                    // Code Editor Area with Line Numbers")

if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + search_ui + "\n" + content[end_idx:]

content = content.replace("CodeVisualTransformation(viewModel.searchQuery)", "CodeVisualTransformation(viewModel.searchQuery, viewModel.isCaseSensitive, localContent.selection)")

if "import androidx.compose.material.icons.filled.KeyboardArrowUp" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Search", "import androidx.compose.material.icons.filled.Search\nimport androidx.compose.material.icons.filled.KeyboardArrowUp\nimport androidx.compose.material.icons.filled.KeyboardArrowDown")

with open("app/src/main/java/com/example/ui/screens/EditorScreen.kt", "w") as f:
    f.write(content)
