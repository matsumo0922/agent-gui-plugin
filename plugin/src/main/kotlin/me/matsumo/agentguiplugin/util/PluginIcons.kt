package me.matsumo.agentguiplugin.util

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object PluginIcons {
    val CLAUDE = load("ic_claude.svg")

    private fun load(name: String): Icon {
        return IconLoader.getIcon("/icons/$name", PluginIcons::class.java)
    }
}
