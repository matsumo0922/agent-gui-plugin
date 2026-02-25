package me.matsumo.agentguiplugin.settings

import androidx.compose.runtime.mutableStateOf
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import me.matsumo.agentguiplugin.service.SettingsService
import org.jetbrains.jewel.bridge.JewelComposePanel
import javax.swing.JComponent

class AgentGuiSettingsConfigurable : Configurable {

    private val settings = service<SettingsService>()

    private val cliPathState = mutableStateOf(settings.claudeCodePath.orEmpty())
    private var originalCliPath = settings.claudeCodePath.orEmpty()

    override fun getDisplayName(): String = "Agent GUI"

    override fun createComponent(): JComponent = JewelComposePanel {
        AgentGuiSettingsPanel(
            cliPath = cliPathState.value,
            onCliPathChange = { cliPathState.value = it },
        )
    }

    override fun isModified(): Boolean =
        cliPathState.value != originalCliPath

    override fun apply() {
        settings.claudeCodePath = cliPathState.value.ifBlank { null }
        originalCliPath = cliPathState.value
    }

    override fun reset() {
        val current = settings.claudeCodePath.orEmpty()
        cliPathState.value = current
        originalCliPath = current
    }
}
