package me.matsumo.agentguiplugin.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.JsonObject
import org.jetbrains.jewel.ui.component.Text

@Composable
fun ToolUseBlock(
    toolName: String,
    inputJson: JsonObject,
    elapsed: Double? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFFEF3C7),
                shape = RoundedCornerShape(8.dp),
            )
            .padding(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = toolName,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF92400E),
            )

            if (elapsed != null) {
                Text(
                    text = " (${String.format("%.1f", elapsed)}s)",
                    fontSize = 12.sp,
                    color = Color(0xFFB45309),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }

        val summary = buildInputSummary(inputJson)
        if (summary.isNotEmpty()) {
            Text(
                text = summary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF78350F),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

private fun buildInputSummary(input: JsonObject): String {
    // Show first few key-value pairs as summary
    return input.entries.take(3).joinToString(", ") { (key, value) ->
        val valueStr = value.toString().take(80)
        "$key: $valueStr"
    }
}
