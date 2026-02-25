package me.matsumo.agentguiplugin.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.intellij.openapi.ui.Messages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    LaunchedEffect(Unit) {
        withContext(Dispatchers.Main) {
            val result = Messages.showYesNoDialog(
                "Clear the current chat?\n(Local history will not be deleted)",
                "Clear Chat",
                Messages.getQuestionIcon(),
            )
            if (result == Messages.YES) onConfirm() else onDismiss()
        }
    }
}
