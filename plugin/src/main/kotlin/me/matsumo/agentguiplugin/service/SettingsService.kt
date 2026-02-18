package me.matsumo.agentguiplugin.service

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(
    name = "AgentGuiPluginSettings",
    storages = [Storage("agent-gui-plugin.xml")],
)
class SettingsService : PersistentStateComponent<SettingsService.State> {

    data class State(
        var nodePath: String? = null,
        var claudeCodePath: String? = null,
        var permissionMode: String = "default",
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    var nodePath: String?
        get() = myState.nodePath
        set(value) { myState.nodePath = value }

    var claudeCodePath: String?
        get() = "/opt/homebrew/bin/claude"
        set(value) { myState.claudeCodePath = value }

    var permissionMode: String
        get() = myState.permissionMode
        set(value) { myState.permissionMode = value }
}
