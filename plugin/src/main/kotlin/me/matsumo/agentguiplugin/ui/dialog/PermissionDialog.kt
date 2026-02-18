package me.matsumo.agentguiplugin.ui.dialog

import com.intellij.openapi.ui.DialogWrapper
import kotlinx.serialization.json.JsonObject
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.Timer

class PermissionDialog(
    private val toolName: String,
    private val toolInput: JsonObject,
    private val timeoutSeconds: Int = 60,
) : DialogWrapper(true) {

    var isAllowed: Boolean = false
        private set

    private var remainingSeconds = timeoutSeconds
    private var countdownTimer: Timer? = null
    private lateinit var countdownLabel: JLabel

    init {
        title = "Tool Permission Request"
        init()
        startCountdown()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(8, 8))
        panel.preferredSize = Dimension(500, 350)

        // Tool name header
        val headerLabel = JLabel("Claude wants to use: $toolName")
        headerLabel.font = headerLabel.font.deriveFont(Font.BOLD, 14f)
        panel.add(headerLabel, BorderLayout.NORTH)

        // Input parameters
        val inputText = JTextArea(toolInput.toString().let { json ->
            // Simple pretty print
            json.replace(",", ",\n  ").replace("{", "{\n  ").replace("}", "\n}")
        })
        inputText.isEditable = false
        inputText.font = Font("Monospaced", Font.PLAIN, 12)
        inputText.lineWrap = true
        inputText.wrapStyleWord = true
        panel.add(JScrollPane(inputText), BorderLayout.CENTER)

        // Countdown
        val bottomPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        countdownLabel = JLabel("Auto-deny in ${remainingSeconds}s")
        bottomPanel.add(countdownLabel)
        panel.add(bottomPanel, BorderLayout.SOUTH)

        return panel
    }

    override fun createActions(): Array<Action> {
        return arrayOf(
            object : DialogWrapperAction("Allow") {
                override fun doAction(e: java.awt.event.ActionEvent?) {
                    isAllowed = true
                    stopCountdown()
                    close(OK_EXIT_CODE)
                }
            },
            object : DialogWrapperAction("Deny") {
                override fun doAction(e: java.awt.event.ActionEvent?) {
                    isAllowed = false
                    stopCountdown()
                    close(CANCEL_EXIT_CODE)
                }
            },
        )
    }

    private fun startCountdown() {
        countdownTimer = Timer(1000) { _ ->
            remainingSeconds--
            if (remainingSeconds <= 0) {
                isAllowed = false
                stopCountdown()
                close(CANCEL_EXIT_CODE)
            } else {
                countdownLabel.text = "Auto-deny in ${remainingSeconds}s"
            }
        }
        countdownTimer?.start()
    }

    private fun stopCountdown() {
        countdownTimer?.stop()
        countdownTimer = null
    }

    override fun dispose() {
        stopCountdown()
        super.dispose()
    }
}
