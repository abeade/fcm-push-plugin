package com.abeade.plugin.fcm.push.settings

import com.abeade.plugin.fcm.push.model.PushTemplate
import com.abeade.plugin.fcm.push.utils.CustomEditorField
import com.abeade.plugin.fcm.push.utils.EMPTY
import com.abeade.plugin.fcm.push.utils.addTextChangeListener
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.intellij.icons.AllIcons
import com.intellij.json.JsonLanguage
import com.intellij.openapi.project.Project
import com.intellij.ui.CollectionListModel
import com.intellij.ui.JBSplitter
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.layout.LCFlags
import com.intellij.ui.layout.panel
import java.awt.BorderLayout
import java.io.File
import java.text.NumberFormat
import javax.swing.*
import javax.swing.event.DocumentListener
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.text.NumberFormatter
import java.io.FileWriter
import com.intellij.internal.statistic.service.fus.beans.FSContent.fromJson
import org.codehaus.plexus.util.FileUtils.filename
import java.io.FileReader
import javax.swing.JOptionPane
import javax.swing.JFrame

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
            exportButton.isEnabled = true
        }
        setRemoveAction {
            templatesListModel.remove(templatesList.selectedIndex)
            exportButton.isEnabled = !templatesListModel.isEmpty
        }
    }
    private val templateNameField = JTextField()
    private val templateDataField = CustomEditorField(JsonLanguage.INSTANCE, project, String.EMPTY)
    private val exportButton = JButton("Export").apply {
        icon = AllIcons.Actions.Download
        addActionListener { onExportTemplates() }
    }
    private val importButton = JButton("Import").apply {
        icon = AllIcons.Actions.Upload
        addActionListener { onImportTemplates() }
    }

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
        templatesListModel.removeAll()
        templatesListModel.add(settingsManager.templates.map { it.copy() })
        updateUIState()
    }

    private fun updateUIState() {
        if (templatesListModel.isEmpty) {
            disableTemplate()
            exportButton.isEnabled = false
        } else {
            enableTemplate()
            templatesList.selectedIndex = 0
            showTemplate(templatesListModel.items.firstOrNull())
            exportButton.isEnabled = true
        }
    }

    private fun createUI() {
        add(createGeneralSettingsPannel(), BorderLayout.PAGE_START)
        add(JBSplitter(false,0.3f, 0.22f, 0.8f).apply {
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
            row {
                cell {
                    importButton(growY)
                    exportButton(growY)
                }
            }
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

    private fun onImportTemplates() {
        val fileDialog = JFileChooser().apply {
            fileSelectionMode = JFileChooser.FILES_ONLY
            fileFilter = FileNameExtensionFilter("JSON Files", "json", "json")
            isAcceptAllFileFilterUsed = false
            dialogTitle = "Select plugin templates file to load"
        }
        if (fileDialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION && fileDialog.selectedFile.exists()) {
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
        if (fileDialog.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
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
