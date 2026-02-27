package me.matsumo.agentguiplugin.ui.component.mention

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.matsumo.agentguiplugin.model.AttachedFile
import me.matsumo.agentguiplugin.util.FilePickerUtil

@Stable
class MentionState(
    private val project: Project,
    private val coroutineScope: CoroutineScope,
) {
    private var cachedFilenames: List<String>? = null
    private var filterJob: Job? = null

    var activeQuery: String? by mutableStateOf(null)
        private set

    var mentionStartIndex: Int by mutableIntStateOf(0)
        private set

    var suggestions: List<AttachedFile> by mutableStateOf(emptyList())
        private set

    var selectedIndex: Int by mutableIntStateOf(0)
        private set

    var confirmedMentions: Set<String> by mutableStateOf(emptySet())
        private set

    val activeMentionRange: IntRange?
        get() {
            val query = activeQuery ?: return null
            return mentionStartIndex until (mentionStartIndex + 1 + query.length)
        }

    fun loadFilenames() {
        coroutineScope.launch {
            cachedFilenames = withContext(Dispatchers.Default) {
                FilePickerUtil.loadAllProjectFilenames(project)
            }
        }
    }

    fun onTextChanged(newValue: TextFieldValue, onAttach: (AttachedFile) -> Unit) {
        val text = newValue.text
        val cursorPos = newValue.selection

        if (!cursorPos.collapsed) {
            dismiss()
            return
        }

        val cursor = cursorPos.start

        // Find @ before cursor
        val atIndex = findAtSymbol(text, cursor)

        if (atIndex == null) {
            dismiss()
            return
        }

        val query = text.substring(atIndex + 1, cursor)

        // Query must not contain whitespace
        if (query.contains(' ') || query.contains('\n')) {
            dismiss()
            return
        }

        activeQuery = query
        mentionStartIndex = atIndex
        selectedIndex = 0

        // Check if manually typed mention matches a confirmed mention
        checkManualMention(text, onAttach)

        // Debounced filter
        filterJob?.cancel()
        filterJob = coroutineScope.launch {
            delay(150)
            filterSuggestions(query)
        }
    }

    private fun checkManualMention(text: String, onAttach: (AttachedFile) -> Unit) {
        val filenames = cachedFilenames ?: return

        // Look for patterns like " @filename " or start-of-text "@filename "
        val regex = Regex("""(?:^|\s)@(\S+)(?:\s)""")
        for (match in regex.findAll(text)) {
            val filename = match.groupValues[1]
            if (filename !in confirmedMentions && filename in filenames) {
                confirmedMentions = confirmedMentions + filename
                coroutineScope.launch {
                    val resolved = withContext(Dispatchers.Default) {
                        FilePickerUtil.resolveFiles(project, listOf(filename))
                    }
                    resolved.firstOrNull()?.let(onAttach)
                }
            }
        }
    }

    private suspend fun filterSuggestions(query: String) {
        if (query.isBlank()) {
            suggestions = emptyList()
            return
        }

        val matched = cachedFilenames
            .orEmpty()
            .filter { it.contains(query, ignoreCase = true) }

        suggestions = withContext(Dispatchers.Default) {
            FilePickerUtil.resolveFiles(project, matched)
        }
    }

    fun selectNext() {
        if (suggestions.isEmpty()) return
        selectedIndex = (selectedIndex + 1) % suggestions.size
    }

    fun selectPrevious() {
        if (suggestions.isEmpty()) return
        selectedIndex = (selectedIndex - 1 + suggestions.size) % suggestions.size
    }

    fun confirmSelection(currentValue: TextFieldValue, index: Int = selectedIndex): Pair<TextFieldValue, AttachedFile>? {
        val query = activeQuery ?: return null
        if (suggestions.isEmpty()) return null
        val file = suggestions[index.coerceIn(suggestions.indices)]

        val text = currentValue.text
        val replaceStart = mentionStartIndex
        val replaceEnd = mentionStartIndex + 1 + query.length // @ + query

        val replacement = "@${file.name} "
        val newText = text.substring(0, replaceStart) + replacement + text.substring(replaceEnd)
        val newCursor = replaceStart + replacement.length

        confirmedMentions = confirmedMentions + file.name
        dismiss()

        return TextFieldValue(
            text = newText,
            selection = TextRange(newCursor),
        ) to file
    }

    fun dismiss() {
        activeQuery = null
        suggestions = emptyList()
        selectedIndex = 0
        filterJob?.cancel()
    }

    fun clearMentions() {
        confirmedMentions = emptySet()
    }

    private fun findAtSymbol(text: String, cursor: Int): Int? {
        if (cursor <= 0) return null

        for (i in (cursor - 1) downTo 0) {
            val ch = text[i]
            if (ch == '@') {
                // @ must be at start of text or preceded by whitespace
                if (i == 0 || text[i - 1].isWhitespace()) {
                    return i
                }
                return null
            }
            if (ch.isWhitespace()) return null
        }
        return null
    }
}
