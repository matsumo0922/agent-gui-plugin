package me.matsumo.agentguiplugin.model

import java.util.*

data class ChatTab(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Empty conversation",
    val createdAt: Long = System.currentTimeMillis(),
)
