package com.abeade.plugin.fcm.push.settings

import com.abeade.plugin.fcm.push.model.PushTemplate
import com.abeade.plugin.fcm.push.ui.SettingsPanel
import com.abeade.plugin.fcm.push.utils.CustomEditorField
import com.abeade.plugin.fcm.push.utils.EMPTY
import com.abeade.plugin.fcm.push.utils.addTextChangeListener
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.intellij.icons.AllIcons
import com.intellij.json.JsonLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.panel.ComponentPanelBuilder
import com.intellij.ui.CollectionListModel
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import java.awt.BorderLayout
import java.io.File
import java.text.NumberFormat
import javax.swing.*
import javax.swing.event.DocumentListener
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.text.NumberFormatter
import java.io.FileWriter
import java.io.FileReader
import javax.swing.JOptionPane
import javax.swing.JFrame
import javax.swing.text.DefaultFormatterFactory

class PushSettingsWrapper(project: Project) {

    private val settingsPanel = SettingsPanel()

    private var nameDocumentListener: DocumentListener? = null
    private var templateDocumentListener: com.intellij.openapi.editor.event.DocumentListener? = null
    private val settingsManager = SettingsManager(project)
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
            settingsPanel.exportButton.isEnabled = true
        }
        setRemoveAction {
            templatesListModel.remove(templatesList.selectedIndex)
            settingsPanel.exportButton.isEnabled = !templatesListModel.isEmpty
        }
    }
    private val templateDataField = CustomEditorField(JsonLanguage.INSTANCE, ProjectManager.getInstance().defaultProject, String.EMPTY)

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
        settingsPanel.adbPortField.formatterFactory = DefaultFormatterFactory(formatter)
        settingsPanel.templateDataPanel.layout = BorderLayout()
        settingsPanel.templateDataPanel.add(templateDataField)
        settingsPanel.useStethoField.apply {
            addActionListener {
                settingsPanel.preferenceKeyField.isEnabled = isSelected
                settingsPanel.preferenceFileField.isEnabled = isSelected
            }
        }
        settingsPanel.exportButton.apply {
            icon = AllIcons.Actions.Download
            addActionListener { onExportTemplates() }
        }
        settingsPanel.importButton.apply {
            icon = AllIcons.Actions.Upload
            addActionListener { onImportTemplates() }
        }
        settingsPanel.templatesPanel.layout = BorderLayout()
        settingsPanel.templatesPanel.add(toolbarDecorator.createPanel())
        settingsPanel.generalSettingsPanel.border = IdeBorderFactory.createTitledBorder("General settings")
        settingsPanel.sethetoSettingsPanel.border = IdeBorderFactory.createTitledBorder("Stetho settings")
        settingsPanel.templatesContainerPanel.border = IdeBorderFactory.createTitledBorder("Templates")
        settingsPanel.templateContentPanel.border = IdeBorderFactory.createTitledBorder("Template content")
        settingsPanel.templatesSplitPanel.border = BorderFactory.createEmptyBorder()
        settingsPanel.preferenceKeyHelpPanel.layout = BorderLayout()
        settingsPanel.preferenceKeyHelpPanel.add(ComponentPanelBuilder.createCommentComponent("Shared preference Key where the app has stored the Firebase Registration ID", false))
        settingsPanel.preferenceFileHelpPanel.layout = BorderLayout()
        settingsPanel.preferenceFileHelpPanel.add(ComponentPanelBuilder.createCommentComponent("Optional setting. Shared preference File where the app has stored the Firebase Registration ID", false))
    }

    val isModified: Boolean
        get() = settingsPanel.useStethoField.isSelected != settingsManager.useStetho ||
                settingsPanel.preferenceKeyField.text != settingsManager.preferenceKey ||
                settingsPanel.preferenceFileField.text != settingsManager.preferenceFile ||
                settingsPanel.authorizationField.text != settingsManager.authorization ||
                settingsPanel.adbPortField.text != settingsManager.adbPort.toString() ||
                settingsManager.templates != templatesListModel.items

    fun createPanel(): JPanel = settingsPanel.mainPanel

    fun apply() {
        settingsManager.useStetho = settingsPanel.useStethoField.isSelected
        settingsManager.preferenceFile = settingsPanel.preferenceFileField.text
        settingsManager.authorization = settingsPanel.authorizationField.text
        settingsManager.preferenceKey = settingsPanel.preferenceKeyField.text
        settingsManager.adbPort = Integer.parseInt(settingsPanel.adbPortField.text)
        settingsManager.templates.clear()
        settingsManager.templates.addAll(templatesListModel.items.map { it.copy() })
    }

    fun reset() {
        settingsPanel.useStethoField.isSelected = settingsManager.useStetho
        settingsPanel.preferenceFileField.text = settingsManager.preferenceFile
        settingsPanel.preferenceKeyField.text = settingsManager.preferenceKey
        settingsPanel.authorizationField.text = settingsManager.authorization
        settingsPanel.adbPortField.text = settingsManager.adbPort.toString()
        templatesListModel.removeAll()
        templatesListModel.add(settingsManager.templates.map { it.copy() })
        updateUIState()
    }

    private fun updateUIState() {
        if (templatesListModel.isEmpty) {
            disableTemplate()
            settingsPanel.exportButton.isEnabled = false
        } else {
            enableTemplate()
            templatesList.selectedIndex = 0
            showTemplate(templatesListModel.items.firstOrNull())
            settingsPanel.exportButton.isEnabled = true
        }
    }

    private fun disableTemplate() {
        templateDataField.isEnabled = false
        settingsPanel.templateNameField.isEnabled = false
    }

    private fun enableTemplate() {
        templateDataField.isEnabled = true
        settingsPanel.templateNameField.isEnabled = true
    }

    private fun showTemplate(template: PushTemplate?) {
        if (template != null) {
            templateDataField.text = template.data
            settingsPanel.templateNameField.text = template.name
            enableTemplate()
        } else {
            templateDataField.text = String.EMPTY
            settingsPanel.templateNameField.text = String.EMPTY
            disableTemplate()
        }
    }

    private fun addTextChangeListeners() {
        nameDocumentListener = settingsPanel.templateNameField.addTextChangeListener(::onNameChange)
        templateDocumentListener = templateDataField.addTextChangeListener(::onTemplateChange)
    }

    private fun removeTextChangeListeners() {
        nameDocumentListener?.let { settingsPanel.templateNameField.document.removeDocumentListener(it) }
        templateDocumentListener?.let { templateDataField.document.removeDocumentListener(it) }
        nameDocumentListener = null
        templateDocumentListener = null
    }

    private fun onNameChange(name: String) {
        templatesListModel.items[templatesList.selectedIndex].name = name
        settingsPanel.templatesPanel.updateUI()
    }

    private fun onTemplateChange(text: String) {
        templatesListModel.items[templatesList.selectedIndex].data = text
    }

    private fun onImportTemplates() {
        val fileDialog = JFileChooser().apply {
            fileSelectionMode = JFileChooser.FILES_ONLY
            fileFilter = FileNameExtensionFilter("JSON Files", "json", "json")
            isAcceptAllFileFilterUsed = false
            dialogTitle = "Select plugin templates file to load"
        }
        if (fileDialog.showOpenDialog(settingsPanel.mainPanel) == JFileChooser.APPROVE_OPTION && fileDialog.selectedFile.exists()) {
            val templatesListType = object : TypeToken<List<PushTemplate>>() { }.type
            try {
                val gson = GsonBuilder().create()
                val reader = JsonReader(FileReader(fileDialog.selectedFile))
                val data = gson.fromJson<List<PushTemplate>>(reader, templatesListType)
                if (data != null && data.isNotEmpty()) {
                    templatesListModel.removeAll()
                    templatesListModel.add(data)
                    updateUIState()
                    JOptionPane.showMessageDialog(
                        JFrame(), "Templates imported successfully", "Templates import",
                        JOptionPane.INFORMATION_MESSAGE
                    )
                } else {
                    JOptionPane.showMessageDialog(JFrame(), "No templates found", "Templates import",
                        JOptionPane.ERROR_MESSAGE)
                }
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(JFrame(), "Error importing templates", "Templates import",
                    JOptionPane.ERROR_MESSAGE)
            }
        }
    }

    private fun onExportTemplates() {
        val fileDialog = JFileChooser().apply {
            fileSelectionMode = JFileChooser.FILES_ONLY
            fileFilter = FileNameExtensionFilter("JSON Files", "json", "json")
            isAcceptAllFileFilterUsed = false
            dialogTitle = "Select plugin templates file to save"
        }
        if (fileDialog.showSaveDialog(settingsPanel.mainPanel) == JFileChooser.APPROVE_OPTION) {
            var file = fileDialog.selectedFile
            if (!file.absolutePath.endsWith("json")) {
                file = File("$file.json")
            }
            try {
                FileWriter(file).use { writer ->
                    val gson = GsonBuilder().create()
                    gson.toJson(templatesListModel.items, writer)
                }
                JOptionPane.showMessageDialog(JFrame(), "Templates exported successfully", "Templates export",
                    JOptionPane.INFORMATION_MESSAGE)
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(JFrame(), "Error exporting templates", "Templates export",
                    JOptionPane.ERROR_MESSAGE)
            }
        }
    }
}
