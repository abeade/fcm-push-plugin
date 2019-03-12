package com.abeade.plugin.fcm.push.settings

import com.abeade.plugin.fcm.push.model.PushTemplate
import com.abeade.plugin.fcm.push.utils.CustomEditorField
import com.abeade.plugin.fcm.push.utils.EMPTY
import com.abeade.plugin.fcm.push.utils.addTextChangeListener
import com.intellij.json.JsonLanguage
import com.intellij.openapi.project.Project
import com.intellij.ui.CollectionListModel
import com.intellij.ui.JBSplitter
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.layout.LCFlags
import com.intellij.ui.layout.panel
import java.awt.BorderLayout
import java.text.NumberFormat
import javax.swing.JFormattedTextField
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.ListSelectionModel
import javax.swing.event.DocumentListener
import javax.swing.text.NumberFormatter

class PushSettingsPanel(project: Project) : JPanel() {

    private var nameDocumentListener: DocumentListener? = null
    private var templateDocumentListener: com.intellij.openapi.editor.event.DocumentListener? = null

    private val settingsManager = SettingsManager(project)
    private val preferenceKeyField = JTextField()
    private val authorizationField = JTextField()
    private val adbPortField: JFormattedTextField
    private val templatesListModel = CollectionListModel<PushTemplate>()
    private val templatesList = JBList<PushTemplate>(templatesListModel).apply {
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        addListSelectionListener {
            if (!it.valueIsAdjusting) {
                if (selectedIndex in 0 until templatesListModel.items.size) {
                    removeTextChangeListeners()
                    showTemplate(templatesListModel.items[selectedIndex])
                    addTextChangeListeners()
                } else {
                    removeTextChangeListeners()
                    showTemplate(null)
                }
            }
        }
    }
    private val toolbarDecorator: ToolbarDecorator = ToolbarDecorator.createDecorator(templatesList).apply {
        setAddAction {
            templatesListModel.add(PushTemplate("Unnamed", String.EMPTY))
            templatesList.selectedIndex = templatesList.itemsCount - 1
        }
        setRemoveAction {
            templatesListModel.remove(templatesList.selectedIndex)
        }
    }
    private val templateNameField = JTextField()
    private val templateDataField = CustomEditorField(JsonLanguage.INSTANCE, project, String.EMPTY)

    init {
        val format = NumberFormat.getInstance().apply {
            isGroupingUsed = false
        }
        val formatter = NumberFormatter(format).apply {
            valueClass = Integer::class.java
            minimum = 0
            maximum = 65535
            allowsInvalid = false
            commitsOnValidEdit = true
        }
        adbPortField = JFormattedTextField(formatter)
        layout = BorderLayout()
        createUI()
    }

    val isModified: Boolean
        get() = preferenceKeyField.text != settingsManager.preferenceKey ||
                authorizationField.text != settingsManager.authorization ||
                adbPortField.text != settingsManager.adbPort!!.toString() ||
                settingsManager.templates != templatesListModel.items

    fun apply() {
        settingsManager.authorization = authorizationField.text
        settingsManager.preferenceKey = preferenceKeyField.text
        settingsManager.adbPort = Integer.parseInt(adbPortField.text)
        settingsManager.templates.clear()
        settingsManager.templates.addAll(templatesListModel.items.map { it.copy() })
    }

    fun reset() {
        preferenceKeyField.text = settingsManager.preferenceKey
        authorizationField.text = settingsManager.authorization
        adbPortField.text = settingsManager.adbPort!!.toString()
        templatesListModel.add(settingsManager.templates.map { it.copy() })
        if (settingsManager.templates.isEmpty()) {
            disableTemplate()
        } else {
            enableTemplate()
            templatesList.selectedIndex = 0
            showTemplate(settingsManager.templates.firstOrNull())
        }
    }

    private fun createUI() {
        add(createGeneralSettingsPannel(), BorderLayout.PAGE_START)
        add(JBSplitter(0.3f).apply {
            firstComponent = createTemplatesListPanel()
            secondComponent = createAndroidComponentsPanel()
        }, BorderLayout.CENTER)
    }

    private fun createGeneralSettingsPannel() =
        panel(LCFlags.fillX, title = "General settings") {
            row("ADB Port") { adbPortField() }
            row("Shared preference Key") { preferenceKeyField() }
            row("") { label("Shared preference where the app has stored the Firebase Registration ID") }
            row("Authorization Key") { authorizationField() }
        }

    private fun createTemplatesListPanel() =
        panel(LCFlags.fillX, title = "Templates") {
            row { toolbarDecorator.createPanel()(growX, growY, pushY) }
        }

    private fun createAndroidComponentsPanel() =
        panel(LCFlags.fillX, title = "Template content") {
            row("Name") { templateNameField() }
            row { label("Data") }
            row { templateDataField(growX, growY, pushY) }
        }

    private fun disableTemplate() {
        templateDataField.isEnabled = false
        templateNameField.isEnabled = false
    }

    private fun enableTemplate() {
        templateDataField.isEnabled = true
        templateNameField.isEnabled = true
    }

    private fun showTemplate(template: PushTemplate?) {
        if (template != null) {
            templateDataField.text = template.data
            templateNameField.text = template.name
            enableTemplate()
        } else {
            templateDataField.text = String.EMPTY
            templateNameField.text = String.EMPTY
            disableTemplate()
        }
    }

    private fun addTextChangeListeners() {
        nameDocumentListener = templateNameField.addTextChangeListener(::onNameChange)
        templateDocumentListener = templateDataField.addTextChangeListener(::onTemplateChange)
    }

    private fun removeTextChangeListeners() {
        nameDocumentListener?.let { templateNameField.document.removeDocumentListener(it) }
        templateDocumentListener?.let { templateDataField.document.removeDocumentListener(it) }
        nameDocumentListener = null
        templateDocumentListener = null
    }

    private fun onNameChange(name: String) {
        templatesListModel.items[templatesList.selectedIndex].name = name
        templatesList.updateUI()
    }

    private fun onTemplateChange(text: String) {
        templatesListModel.items[templatesList.selectedIndex].data = text
    }
}
