import sys

content = """package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class CodeVisualTransformation(
    private val searchQuery: String = "",
    private val isCaseSensitive: Boolean = false,
    private val selection: TextRange = TextRange.Zero
) : VisualTransformation {

    companion object {
        val keywordPattern = "\\\\b(auto|break|case|char|const|continue|default|do|double|else|enum|extern|float|for|goto|if|int|long|register|return|short|signed|sizeof|static|struct|switch|typedef|union|unsigned|void|volatile|while)\\\\b".toRegex()
        val typePattern = "\\\\b(int|float|double|char|void|long|short|unsigned|signed|bool|size_t|FILE)\\\\b".toRegex()
        val stringPattern = "\\".*?\\"".toRegex()
        val charPattern = "'.*?'".toRegex()
        val singleLineCommentPattern = "//.*".toRegex()
        val multiLineCommentPattern = "/\\\\*.*?\\\\*/".toRegex(RegexOption.DOT_MATCHES_ALL)
        val numberPattern = "\\\\b\\\\d+(\\\\.\\\\d+)?\\\\b".toRegex()
        val preprocessorPattern = "^\\\\s*#(include|define|ifndef|endif|ifdef|pragma|if|elif|else)\\\\b.*".toRegex(RegexOption.MULTILINE)
        val operatorPattern = "[+\\\\-*/%=<>!&|\\\\^~]+".toRegex()
        
        val BracketPairs = mapOf('(' to ')', '{' to '}', '[' to ']')
        val ReverseBracketPairs = mapOf(')' to '(', '}' to '{', ']' to '[')
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val inputText = text.text
        val annotatedString = buildAnnotatedString {
            append(inputText)
            
            // 3. Current Line Highlight
            if (selection.start == selection.end && selection.start in 0..inputText.length) {
                val startOfLine = inputText.lastIndexOf('\\n', maxOf(0, selection.start - 1)).let { if (it == -1) 0 else it + 1 }
                val endOfLine = inputText.indexOf('\\n', selection.start).let { if (it == -1) inputText.length else it }
                if (startOfLine <= endOfLine) {
                    addStyle(SpanStyle(background = Color.White.copy(alpha = 0.05f)), startOfLine, endOfLine + (if (endOfLine < inputText.length) 1 else 0))
                }
            }

            // 4. Highlight Selected Word
            if (selection.start == selection.end && selection.start in 0..inputText.length) {
                val wordRegex = "\\\\b[a-zA-Z_]\\\\w*\\\\b".toRegex()
                val matches = wordRegex.findAll(inputText)
                var selectedWord: String? = null
                for (match in matches) {
                    if (selection.start in match.range.first..match.range.last + 1) {
                        selectedWord = match.value
                        break
                    }
                }
                if (selectedWord != null) {
                    val allMatches = "\\\\b$selectedWord\\\\b".toRegex().findAll(inputText)
                    for (match in allMatches) {
                        addStyle(SpanStyle(background = Color.White.copy(alpha = 0.1f)), match.range.first, match.range.last + 1)
                    }
                }
            }

            // Syntax Highlighting
            operatorPattern.findAll(inputText).forEach { match ->
                addStyle(SpanStyle(color = Color(0xFF56B6C2)), match.range.first, match.range.last + 1)
            }
            numberPattern.findAll(inputText).forEach { match ->
                addStyle(SpanStyle(color = Color(0xFFD19A66)), match.range.first, match.range.last + 1)
            }
            keywordPattern.findAll(inputText).forEach { match ->
                addStyle(SpanStyle(color = Color(0xFFC678DD)), match.range.first, match.range.last + 1)
            }
            typePattern.findAll(inputText).forEach { match ->
                addStyle(SpanStyle(color = Color(0xFFE5C07B)), match.range.first, match.range.last + 1)
            }
            preprocessorPattern.findAll(inputText).forEach { match ->
                addStyle(SpanStyle(color = Color(0xFFC678DD)), match.range.first, match.range.last + 1)
            }
            stringPattern.findAll(inputText).forEach { match ->
                addStyle(SpanStyle(color = Color(0xFF98C379)), match.range.first, match.range.last + 1)
            }
            charPattern.findAll(inputText).forEach { match ->
                addStyle(SpanStyle(color = Color(0xFF98C379)), match.range.first, match.range.last + 1)
            }
            singleLineCommentPattern.findAll(inputText).forEach { match ->
                addStyle(SpanStyle(color = Color(0xFF5C6370)), match.range.first, match.range.last + 1)
            }
            multiLineCommentPattern.findAll(inputText).forEach { match ->
                addStyle(SpanStyle(color = Color(0xFF5C6370)), match.range.first, match.range.last + 1)
            }

            // 2. Bracket Matching
            if (selection.start == selection.end && selection.start in 0..inputText.length) {
                var bracketIdx = -1
                var isOpening = false
                
                if (selection.start < inputText.length && BracketPairs.containsKey(inputText[selection.start])) {
                    bracketIdx = selection.start
                    isOpening = true
                } else if (selection.start > 0 && BracketPairs.containsKey(inputText[selection.start - 1])) {
                    bracketIdx = selection.start - 1
                    isOpening = true
                } else if (selection.start < inputText.length && ReverseBracketPairs.containsKey(inputText[selection.start])) {
                    bracketIdx = selection.start
                    isOpening = false
                } else if (selection.start > 0 && ReverseBracketPairs.containsKey(inputText[selection.start - 1])) {
                    bracketIdx = selection.start - 1
                    isOpening = false
                }
                
                if (bracketIdx != -1) {
                    val char = inputText[bracketIdx]
                    val matchingChar = if (isOpening) BracketPairs[char]!! else ReverseBracketPairs[char]!!
                    val dir = if (isOpening) 1 else -1
                    var depth = 1
                    var matchIdx = -1
                    var i = bracketIdx + dir
                    while (i in 0 until inputText.length && i >= 0) {
                        if (inputText[i] == char) depth++
                        else if (inputText[i] == matchingChar) depth--
                        
                        if (depth == 0) {
                            matchIdx = i
                            break
                        }
                        i += dir
                    }
                    
                    val bracketStyle = SpanStyle(background = Color.LightGray.copy(alpha = 0.3f), color = Color.White)
                    addStyle(bracketStyle, bracketIdx, bracketIdx + 1)
                    if (matchIdx != -1) {
                        addStyle(bracketStyle, matchIdx, matchIdx + 1)
                    }
                }
            }

            // Search Query Highlighting
            if (searchQuery.isNotBlank()) {
                var index = inputText.indexOf(searchQuery, ignoreCase = !isCaseSensitive)
                while (index >= 0) {
                    addStyle(
                        SpanStyle(background = Color.Yellow.copy(alpha = 0.5f), color = Color.Black),
                        index,
                        index + searchQuery.length
                    )
                    index = inputText.indexOf(searchQuery, index + searchQuery.length, ignoreCase = !isCaseSensitive)
                }
            }
        }
        return TransformedText(annotatedString, OffsetMapping.Identity)
    }
}
"""

with open("app/src/main/java/com/example/ui/components/CodeVisualTransformation.kt", "w") as f:
    f.write(content)
