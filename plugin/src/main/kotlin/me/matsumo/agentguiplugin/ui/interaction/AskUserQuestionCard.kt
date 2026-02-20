package me.matsumo.agentguiplugin.ui.interaction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.matsumo.agentguiplugin.ui.theme.ChatTheme
import me.matsumo.agentguiplugin.viewmodel.PendingQuestion
import org.jetbrains.jewel.ui.component.Text

private val questionAccentColor = Color(0xFF3B82F6)

private data class ParsedOption(val label: String, val description: String)

private data class ParsedQuestion(
    val question: String,
    val isMultiSelect: Boolean,
    val options: List<ParsedOption>,
)

@Composable
fun AskUserQuestionCard(
    question: PendingQuestion,
    onSubmit: (Map<String, String>) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val parsedQuestions = remember(question) { parseQuestions(question.toolInput) }
    val selectedOptions = remember(question) { parsedQuestions.map { mutableStateOf(setOf<Int>()) } }
    val otherTexts = remember(question) { parsedQuestions.map { mutableStateOf("") } }

    val cardShape = RoundedCornerShape(ChatTheme.Radius.large)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, questionAccentColor.copy(alpha = 0.35f), cardShape)
            .background(questionAccentColor.copy(alpha = 0.03f), cardShape),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(questionAccentColor.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, questionAccentColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "?", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = questionAccentColor)
            }
            Text(
                text = "Claude has a question",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChatTheme.Text.primary,
            )
        }

        // Questions (scrollable if many)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 380.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            parsedQuestions.forEachIndexed { qIndex, q ->
                QuestionSection(
                    question = q,
                    selectedIndices = selectedOptions[qIndex].value,
                    otherText = otherTexts[qIndex].value,
                    onOptionToggle = { optIdx ->
                        if (q.isMultiSelect) {
                            val current = selectedOptions[qIndex].value
                            selectedOptions[qIndex].value =
                                if (optIdx in current) current - optIdx else current + optIdx
                        } else {
                            selectedOptions[qIndex].value = setOf(optIdx)
                        }
                    },
                    onOtherTextChange = { otherTexts[qIndex].value = it },
                    modifier = Modifier.padding(horizontal = 14.dp),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Submit / Cancel buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Submit button
            Box(
                modifier = Modifier
                    .background(
                        questionAccentColor.copy(alpha = 0.12f),
                        RoundedCornerShape(ChatTheme.Radius.medium),
                    )
                    .border(
                        1.dp,
                        questionAccentColor.copy(alpha = 0.5f),
                        RoundedCornerShape(ChatTheme.Radius.medium),
                    )
                    .clickable {
                        val answers = parsedQuestions.mapIndexed { qIndex, q ->
                            val selected = selectedOptions[qIndex].value
                                .sorted()
                                .map { i -> q.options[i].label }
                                .toMutableList()
                            val other = otherTexts[qIndex].value.trim()
                            if (other.isNotEmpty()) selected.add(other)
                            q.question to selected.joinToString(", ")
                        }.toMap()
                        onSubmit(answers)
                    }
                    .padding(horizontal = 16.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Submit",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = questionAccentColor,
                )
            }

            // Cancel button
            val cancelBorder = ChatTheme.Border.default
            Box(
                modifier = Modifier
                    .border(1.dp, cancelBorder, RoundedCornerShape(ChatTheme.Radius.medium))
                    .clickable(onClick = onCancel)
                    .padding(horizontal = 16.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "Cancel", fontSize = 12.sp, color = ChatTheme.Text.muted)
            }
        }
    }
}

@Composable
private fun QuestionSection(
    question: ParsedQuestion,
    selectedIndices: Set<Int>,
    otherText: String,
    onOptionToggle: (Int) -> Unit,
    onOtherTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedBg = questionAccentColor.copy(alpha = 0.10f)
    val selectedBorder = questionAccentColor.copy(alpha = 0.45f)
    val defaultBg = ChatTheme.Background.muted
    val defaultBorder = ChatTheme.Border.default

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        // Question text
        Text(
            text = question.question,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = ChatTheme.Text.primary,
        )

        if (question.isMultiSelect) {
            Text(
                text = "Select all that apply",
                fontSize = 10.sp,
                color = ChatTheme.Text.muted,
            )
        }

        // Option rows
        question.options.forEachIndexed { idx, option ->
            val isSelected = idx in selectedIndices
            val optionShape = RoundedCornerShape(ChatTheme.Radius.medium)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isSelected) selectedBg else defaultBg, optionShape)
                    .border(1.dp, if (isSelected) selectedBorder else defaultBorder, optionShape)
                    .clickable { onOptionToggle(idx) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Selection indicator (radio circle or checkbox square)
                if (question.isMultiSelect) {
                    CheckboxIndicator(isSelected = isSelected)
                } else {
                    RadioIndicator(isSelected = isSelected)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = option.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = if (isSelected) ChatTheme.Text.primary else ChatTheme.Text.secondary,
                    )
                    if (option.description.isNotEmpty()) {
                        Text(
                            text = option.description,
                            fontSize = 10.sp,
                            color = ChatTheme.Text.muted,
                        )
                    }
                }
            }
        }

        // Other: free text input (always visible)
        val textColor = ChatTheme.Text.primary
        val placeholderColor = ChatTheme.Input.placeholder
        val otherBorder = if (otherText.isNotEmpty()) {
            questionAccentColor.copy(alpha = 0.45f)
        } else {
            defaultBorder
        }
        val otherShape = RoundedCornerShape(ChatTheme.Radius.medium)

        BasicTextField(
            value = otherText,
            onValueChange = onOtherTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, otherBorder, otherShape)
                .background(defaultBg, otherShape)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            textStyle = TextStyle(fontSize = 12.sp, color = textColor),
            cursorBrush = SolidColor(textColor),
            decorationBox = { innerTextField ->
                Box {
                    if (otherText.isEmpty()) {
                        Text(
                            text = "Other (free text)...",
                            fontSize = 12.sp,
                            color = placeholderColor,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun RadioIndicator(isSelected: Boolean) {
    val borderColor = if (isSelected) questionAccentColor else ChatTheme.Border.default
    Box(
        modifier = Modifier
            .size(14.dp)
            .border(1.5.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(questionAccentColor, CircleShape),
            )
        }
    }
}

@Composable
private fun CheckboxIndicator(isSelected: Boolean) {
    val borderColor = if (isSelected) questionAccentColor else ChatTheme.Border.default
    val checkboxShape = RoundedCornerShape(3.dp)
    Box(
        modifier = Modifier
            .size(14.dp)
            .border(1.5.dp, borderColor, checkboxShape)
            .background(
                if (isSelected) questionAccentColor.copy(alpha = 0.2f) else Color.Transparent,
                checkboxShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Text(text = "✓", fontSize = 8.sp, color = questionAccentColor)
        }
    }
}

private fun parseQuestions(toolInput: Map<String, Any?>): List<ParsedQuestion> {
    val questionsRaw = toolInput["questions"] as? List<*> ?: return emptyList()
    return questionsRaw.mapNotNull { qAny ->
        val qMap = qAny as? Map<*, *> ?: return@mapNotNull null
        val text = qMap["question"] as? String ?: return@mapNotNull null
        val isMultiSelect = qMap["multiSelect"] as? Boolean ?: false
        val options = (qMap["options"] as? List<*>)?.mapNotNull { optAny ->
            val optMap = optAny as? Map<*, *> ?: return@mapNotNull null
            val label = optMap["label"] as? String ?: return@mapNotNull null
            val description = optMap["description"] as? String ?: ""
            ParsedOption(label, description)
        } ?: emptyList()
        ParsedQuestion(text, isMultiSelect, options)
    }
}
