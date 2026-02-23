package me.matsumo.agentguiplugin.service

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import me.matsumo.claude.agent.types.Model
import me.matsumo.claude.agent.types.PermissionMode

@Service(Service.Level.APP)
@State(
    name = "AgentGuiPluginSettings",
    storages = [Storage("agent-gui-plugin.xml")],
)
class SettingsService : PersistentStateComponent<SettingsService.State> {

    data class State(
        var claudeCodePath: String? = null,
        var permissionMode: String = "default",
        var model: String = "sonnet",
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    var claudeCodePath: String?
        get() = myState.claudeCodePath
        set(value) { myState.claudeCodePath = value }

    var permissionMode: PermissionMode
        get() = PermissionMode.entries.find { it.modeId == myState.permissionMode } ?: PermissionMode.DEFAULT
        set(value) { myState.permissionMode = value.modeId }

    var model: Model
        get() = Model.entries.find { it.modelId == myState.model } ?: Model.SONNET
        set(value) { myState.model = value.modelId }
}
