package me.matsumo.agentguiplugin.viewmodel.permission

internal object ToolNames {
    const val ASK_USER_QUESTION = "AskUserQuestion"
    const val ENTER_PLAN_MODE = "EnterPlanMode"
    const val EXIT_PLAN_MODE = "ExitPlanMode"

    val EDIT_TOOL_NAMES = setOf("Edit", "str_replace_based_edit_tool", "StrReplaceBasedEditTool")
    val WRITE_TOOL_NAMES = setOf("Write", "write_file", "create_file")
}
