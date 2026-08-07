package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

data class SyntaxColors(
    val operator: Color,
    val number: Color,
    val keyword: Color,
    val type: Color,
    val preprocessor: Color,
    val string: Color,
    val comment: Color,
    val currentLineBackground: Color,
    val selectedWordBackground: Color,
    val bracketMatchBackground: Color,
    val bracketMatchText: Color
)

val LightSyntaxColors = SyntaxColors(
    operator = Color(0xFF005CC5),
    number = Color(0xFF005CC5),
    keyword = Color(0xFFD73A49),
    type = Color(0xFFD73A49),
    preprocessor = Color(0xFFD73A49),
    string = Color(0xFF032F62),
    comment = Color(0xFF6A737D),
    currentLineBackground = Color.Black.copy(alpha = 0.05f),
    selectedWordBackground = Color.Black.copy(alpha = 0.1f),
    bracketMatchBackground = Color.Black.copy(alpha = 0.2f),
    bracketMatchText = Color.Black
)

val DarkSyntaxColors = SyntaxColors(
    operator = Color(0xFF56B6C2),
    number = Color(0xFFD19A66),
    keyword = Color(0xFFC678DD),
    type = Color(0xFFE5C07B),
    preprocessor = Color(0xFFC678DD),
    string = Color(0xFF98C379),
    comment = Color(0xFF5C6370),
    currentLineBackground = Color.White.copy(alpha = 0.05f),
    selectedWordBackground = Color.White.copy(alpha = 0.1f),
    bracketMatchBackground = Color.White.copy(alpha = 0.3f),
    bracketMatchText = Color.White
)

val AmoledSyntaxColors = DarkSyntaxColors.copy(
    currentLineBackground = Color.White.copy(alpha = 0.1f),
    selectedWordBackground = Color.White.copy(alpha = 0.15f),
    bracketMatchBackground = Color.White.copy(alpha = 0.2f)
)

val MidnightSyntaxColors = DarkSyntaxColors.copy(
    operator = Color(0xFF80CBC4),
    number = Color(0xFFF78C6C),
    keyword = Color(0xFFC792EA),
    type = Color(0xFFFFCB6B),
    preprocessor = Color(0xFFC792EA),
    string = Color(0xFFC3E88D),
    comment = Color(0xFF546E7A)
)

class CodeVisualTransformation(
    private val searchQuery: String = "",
    private val isCaseSensitive: Boolean = false,
    private val selection: TextRange = TextRange.Zero,
    private val syntaxColors: SyntaxColors = DarkSyntaxColors
) : VisualTransformation {
    companion object {
        val keywordPattern = "\\b(auto|break|case|char|const|continue|default|do|double|else|enum|extern|float|for|goto|if|int|long|register|return|short|signed|sizeof|static|struct|switch|typedef|union|unsigned|void|volatile|while)\\b".toRegex()
        val typePattern = "\\b(int|float|double|char|void|long|short|unsigned|signed|bool|size_t|FILE)\\b".toRegex()
        val stringPattern = "\".*?\"".toRegex()
        val charPattern = "'.*?'".toRegex()
        val singleLineCommentPattern = "//.*".toRegex()
        val multiLineCommentPattern = "/\\*.*?\\*/".toRegex(RegexOption.DOT_MATCHES_ALL)
        val numberPattern = "\\b\\d+(\\.\\d+)?\\b".toRegex()
        val preprocessorPattern = "^\\s*#(include|define|ifndef|endif|ifdef|pragma|if|elif|else)\\b.*".toRegex(RegexOption.MULTILINE)
        val operatorPattern = "[+\\-*/%=<>!&|\\^~]+".toRegex()
        
        val BracketPairs = mapOf('(' to ')', '{' to '}', '[' to ']')
        val ReverseBracketPairs = mapOf(')' to '(', '}' to '{', ']' to '[')
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val inputText = text.text
        val annotatedString = buildAnnotatedString {
            append(inputText)
            
            // 3. Current Line Highlight
            if (selection.start == selection.end && selection.start in 0..inputText.length) {
                val startOfLine = inputText.lastIndexOf('\n', maxOf(0, selection.start - 1)).let { if (it == -1) 0 else it + 1 }
                val endOfLine = inputText.indexOf('\n', selection.start).let { if (it == -1) inputText.length else it }
                if (startOfLine <= endOfLine) {
                    addStyle(SpanStyle(background = syntaxColors.currentLineBackground), startOfLine, endOfLine + (if (endOfLine < inputText.length) 1 else 0))
                }
            }

            // 4. Highlight Selected Word
            if (selection.start == selection.end && selection.start in 0..inputText.length) {
                val wordRegex = "\\b[a-zA-Z_]\\w*\\b".toRegex()
                val matches = wordRegex.findAll(inputText)
                var selectedWord: String? = null
                for (match in matches) {
                    if (selection.start in match.range.first..match.range.last + 1) {
                        selectedWord = match.value
                        break
                    }
                }
                if (selectedWord != null) {
                    val allMatches = "\\b$selectedWord\\b".toRegex().findAll(inputText)
                    for (match in allMatches) {
                        addStyle(SpanStyle(background = syntaxColors.selectedWordBackground), match.range.first, match.range.last + 1)
                    }
                }
            }

            // Syntax Highlighting
            operatorPattern.findAll(inputText).forEach { match ->
                addStyle(SpanStyle(color = syntaxColors.operator), match.range.first, match.range.last + 1)
            }
            numberPattern.findAll(inputText).forEach { match ->
                addStyle(SpanStyle(color = syntaxColors.number), match.range.first, match.range.last + 1)
            }
            keywordPattern.findAll(inputText).forEach { match ->
                addStyle(SpanStyle(color = syntaxColors.keyword), match.range.first, match.range.last + 1)
            }
            typePattern.findAll(inputText).forEach { match ->
                addStyle(SpanStyle(color = syntaxColors.type), match.range.first, match.range.last + 1)
            }
            preprocessorPattern.findAll(inputText).forEach { match ->
                addStyle(SpanStyle(color = syntaxColors.preprocessor), match.range.first, match.range.last + 1)
            }
            stringPattern.findAll(inputText).forEach { match ->
                addStyle(SpanStyle(color = syntaxColors.string), match.range.first, match.range.last + 1)
            }
            charPattern.findAll(inputText).forEach { match ->
                addStyle(SpanStyle(color = syntaxColors.string), match.range.first, match.range.last + 1)
            }
            singleLineCommentPattern.findAll(inputText).forEach { match ->
                addStyle(SpanStyle(color = syntaxColors.comment), match.range.first, match.range.last + 1)
            }
            multiLineCommentPattern.findAll(inputText).forEach { match ->
                addStyle(SpanStyle(color = syntaxColors.comment), match.range.first, match.range.last + 1)
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
                    
                    val bracketStyle = SpanStyle(background = syntaxColors.bracketMatchBackground, color = syntaxColors.bracketMatchText)
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
