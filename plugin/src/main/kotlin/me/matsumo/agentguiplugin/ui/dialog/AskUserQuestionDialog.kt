package me.matsumo.agentguiplugin.ui.dialog

import com.intellij.openapi.ui.DialogWrapper
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.border.EmptyBorder

class AskUserQuestionDialog(
    private val toolInput: JsonObject,
) : DialogWrapper(true) {

    val answers: MutableMap<String, String> = mutableMapOf()
    private val questionPanels = mutableListOf<QuestionPanel>()

    init {
        title = "Claude has a question"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.preferredSize = Dimension(550, 400)

        val questions = toolInput["questions"]?.jsonArray ?: JsonArray(emptyList())

        questions.forEach { questionElement ->
            val questionObj = questionElement.jsonObject
            val questionText = questionObj["question"]?.jsonPrimitive?.content ?: ""
            val isMultiSelect = questionObj["multiSelect"]?.jsonPrimitive?.boolean ?: false
            val options = questionObj["options"]?.jsonArray?.map { opt ->
                val optObj = opt.jsonObject
                val label = optObj["label"]?.jsonPrimitive?.content ?: ""
                val description = optObj["description"]?.jsonPrimitive?.content ?: ""
                QuestionOption(label, description)
            } ?: emptyList()

            val qPanel = QuestionPanel(questionText, options, isMultiSelect)
            questionPanels.add(qPanel)
            panel.add(qPanel.panel)
        }

        return JScrollPane(panel)
    }

    override fun doOKAction() {
        questionPanels.forEach { qPanel ->
            answers[qPanel.questionText] = qPanel.getSelectedAnswer()
        }
        super.doOKAction()
    }

    private data class QuestionOption(val label: String, val description: String)

    private class QuestionPanel(
        val questionText: String,
        private val options: List<QuestionOption>,
        private val isMultiSelect: Boolean,
    ) {
        val panel: JPanel = JPanel()
        private val radioButtons = mutableListOf<JRadioButton>()
        private val checkBoxes = mutableListOf<JCheckBox>()
        private val otherTextField = JTextField(20)
        private val otherRadioButton = JRadioButton("Other")
        private val otherCheckBox = JCheckBox("Other")

        init {
            panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
            panel.border = EmptyBorder(8, 8, 16, 8)

            // Question label
            val label = JLabel(questionText)
            label.font = label.font.deriveFont(Font.BOLD, 13f)
            label.alignmentX = JComponent.LEFT_ALIGNMENT

            val labelPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
            labelPanel.add(label)
            labelPanel.alignmentX = JComponent.LEFT_ALIGNMENT
            panel.add(labelPanel)

            if (isMultiSelect) {
                options.forEach { opt ->
                    val cb = JCheckBox("${opt.label} - ${opt.description}")
                    checkBoxes.add(cb)
                    val cbPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
                    cbPanel.add(cb)
                    cbPanel.alignmentX = JComponent.LEFT_ALIGNMENT
                    panel.add(cbPanel)
                }
                val otherPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
                otherPanel.add(otherCheckBox)
                otherPanel.add(otherTextField)
                otherPanel.alignmentX = JComponent.LEFT_ALIGNMENT
                panel.add(otherPanel)
            } else {
                val group = ButtonGroup()
                options.forEach { opt ->
                    val rb = JRadioButton("${opt.label} - ${opt.description}")
                    radioButtons.add(rb)
                    group.add(rb)
                    val rbPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
                    rbPanel.add(rb)
                    rbPanel.alignmentX = JComponent.LEFT_ALIGNMENT
                    panel.add(rbPanel)
                }
                group.add(otherRadioButton)
                val otherPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
                otherPanel.add(otherRadioButton)
                otherPanel.add(otherTextField)
                otherPanel.alignmentX = JComponent.LEFT_ALIGNMENT
                panel.add(otherPanel)
            }
        }

        fun getSelectedAnswer(): String {
            return if (isMultiSelect) {
                val selected = checkBoxes.mapIndexedNotNull { index, cb ->
                    if (cb.isSelected) options[index].label else null
                }.toMutableList()
                if (otherCheckBox.isSelected && otherTextField.text.isNotBlank()) {
                    selected.add(otherTextField.text.trim())
                }
                selected.joinToString(", ")
            } else {
                val selectedIndex = radioButtons.indexOfFirst { it.isSelected }
                if (selectedIndex >= 0) {
                    options[selectedIndex].label
                } else if (otherRadioButton.isSelected && otherTextField.text.isNotBlank()) {
                    otherTextField.text.trim()
                } else {
                    ""
                }
            }
        }
    }
}
