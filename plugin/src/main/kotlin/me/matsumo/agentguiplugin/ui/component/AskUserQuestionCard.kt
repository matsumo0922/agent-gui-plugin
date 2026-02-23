package me.matsumo.agentguiplugin.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.matsumo.agentguiplugin.viewmodel.PendingQuestion
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.theme.colorPalette
import org.jetbrains.jewel.ui.typography

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

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = questionAccentColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp),
            )
            .background(questionAccentColor.copy(alpha = 0.04f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HeaderSection(
            modifier = Modifier.fillMaxWidth(),
            questionCount = parsedQuestions.size,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 240.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            parsedQuestions.forEachIndexed { index, question ->
                QuestionSection(
                    modifier = Modifier.fillMaxWidth(),
                    question = question,
                    selectedIndices = selectedOptions[index].value,
                    otherText = otherTexts[index].value,
                    onOptionToggle = { optIdx ->
                        if (question.isMultiSelect) {
                            val current = selectedOptions[index].value
                            selectedOptions[index].value =
                                if (optIdx in current) current - optIdx else current + optIdx
                        } else {
                            selectedOptions[index].value = setOf(optIdx)
                        }
                    },
                    onOtherTextChange = { otherTexts[index].value = it },
                )
            }
        }

        val isAllAnswered = parsedQuestions.indices.all { qIndex ->
            selectedOptions[qIndex].value.isNotEmpty() || otherTexts[qIndex].value.trim().isNotEmpty()
        }

        ButtonSection(
            modifier = Modifier.fillMaxWidth(),
            submitEnabled = isAllAnswered,
            onSubmit = {
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
            },
            onCancel = onCancel,
        )
    }
}

@Composable
private fun HeaderSection(
    questionCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(questionAccentColor.copy(alpha = 0.15f), CircleShape)
                .border(1.dp, questionAccentColor.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "?",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = questionAccentColor,
            )
        }

        Text(
            text = if (questionCount <= 1) "Claude has a question" else "Claude has $questionCount questions",
            style = JewelTheme.typography.regular,
            fontWeight = FontWeight.SemiBold,
        )
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
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            modifier = Modifier.padding(vertical = 4.dp),
            text = buildAnnotatedString {
                append(question.question)

                if (question.isMultiSelect) {
                    withStyle(
                        JewelTheme.typography.small.copy(
                            color = JewelTheme.globalColors.text.info
                        ).toSpanStyle()
                    ) {
                        append(" (複数選択可能)")
                    }
                }
            },
            style = JewelTheme.typography.medium,
            color = JewelTheme.globalColors.text.normal,
            fontWeight = FontWeight.SemiBold,
        )

        question.options.forEachIndexed { index, option ->
            val isSelected = index in selectedIndices

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isSelected) questionAccentColor.copy(alpha = 0.10f) else JewelTheme.colorPalette.gray(2))
                    .border(
                        width = 1.dp,
                        color = if (isSelected) questionAccentColor.copy(alpha = 0.45f) else JewelTheme.globalColors.borders.normal,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable { onOptionToggle(index) }
                    .padding(12.dp, 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (question.isMultiSelect) {
                    CheckboxIndicator(isSelected = isSelected)
                } else {
                    RadioIndicator(isSelected = isSelected)
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = option.label,
                        style = JewelTheme.typography.medium,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = JewelTheme.globalColors.text.normal
                    )

                    if (option.description.isNotEmpty()) {
                        Text(
                            text = option.description,
                            style = JewelTheme.typography.medium,
                            color = JewelTheme.globalColors.text.info,
                        )
                    }
                }
            }
        }

        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .border(
                    width = 1.dp,
                    color = if (otherText.isNotEmpty()) questionAccentColor.copy(alpha = 0.45f) else JewelTheme.globalColors.borders.normal,
                    shape = RoundedCornerShape(4.dp)
                )
                .background(JewelTheme.colorPalette.gray(2))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            value = otherText,
            onValueChange = onOtherTextChange,
            textStyle = JewelTheme.typography.medium.copy(color = JewelTheme.globalColors.text.normal),
            cursorBrush = SolidColor(JewelTheme.globalColors.text.normal),
            decorationBox = { innerTextField ->
                Box {
                    if (otherText.isEmpty()) {
                        Text(
                            text = "Other (free text)...",
                            style = JewelTheme.typography.medium,
                            color = JewelTheme.globalColors.text.info,
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
    val borderColor = if (isSelected) questionAccentColor else JewelTheme.globalColors.text.info

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
    val borderColor = if (isSelected) questionAccentColor else  JewelTheme.globalColors.text.info
    val checkboxShape = RoundedCornerShape(3.dp)

    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(checkboxShape)
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = checkboxShape
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Text(
                text = "✓",
                fontSize = 8.sp,
                color = questionAccentColor
            )
        }
    }
}

@Composable
private fun ButtonSection(
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    submitEnabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            text = "Submit",
            onClick = onSubmit,
            borderColor = questionAccentColor.copy(alpha = 0.5f),
            backgroundColor = questionAccentColor.copy(alpha = 0.15f),
            textColor = JewelTheme.globalColors.text.normal,
            enabled = submitEnabled,
        )

        Button(
            text = "Cancel",
            onClick = onCancel,
            borderColor = JewelTheme.globalColors.borders.normal,
            textColor = JewelTheme.globalColors.text.info,
        )
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
