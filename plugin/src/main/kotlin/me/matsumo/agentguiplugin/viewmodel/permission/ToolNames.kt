package me.matsumo.agentguiplugin.viewmodel.permission

internal object ToolNames {
    const val ASK_USER_QUESTION = "AskUserQuestion"
    const val ENTER_PLAN_MODE = "EnterPlanMode"
    const val EXIT_PLAN_MODE = "ExitPlanMode"

    val READ_TOOL_NAMES = setOf("Read", "read_file")
    val EDIT_TOOL_NAMES = setOf("Edit", "str_replace_based_edit_tool", "StrReplaceBasedEditTool")
    val WRITE_TOOL_NAMES = setOf("Write", "write_file", "create_file")

    /** result を UI に表示しないツール名（入出力が大きいため） */
    val RESULT_IGNORED_TOOL_NAMES = READ_TOOL_NAMES + EDIT_TOOL_NAMES + WRITE_TOOL_NAMES
}
