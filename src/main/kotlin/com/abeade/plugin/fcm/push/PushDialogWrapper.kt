package com.abeade.plugin.fcm.push

import com.abeade.plugin.fcm.push.model.PushData
import com.abeade.plugin.fcm.push.settings.SettingsManager
import com.abeade.plugin.fcm.push.stetho.HumanReadableException
import com.abeade.plugin.fcm.push.stetho.MultipleDevicesException
import com.abeade.plugin.fcm.push.stetho.MultipleStethoProcessesException
import com.abeade.plugin.fcm.push.stetho.StethoPreferenceSearcher
import com.abeade.plugin.fcm.push.ui.PushDialog
import com.abeade.plugin.fcm.push.utils.CustomEditorField
import com.abeade.plugin.fcm.push.utils.EMPTY
import com.abeade.plugin.fcm.push.utils.showNotification
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.intellij.icons.AllIcons
import com.intellij.ide.util.PropertiesComponent
import com.intellij.json.JsonLanguage
import com.intellij.notification.NotificationType
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import java.awt.BorderLayout
import java.awt.event.ItemEvent
import javax.swing.ButtonGroup
import javax.swing.JComponent


class PushDialogWrapper(
    private val propertiesComponent: PropertiesComponent,
    private val project: Project
) : DialogWrapper(true) {

    companion object {
        private const val FIREBASE_KEY = "#com.abeade.plugin.fcm.push.pushdialog.firebase"
        private const val DATA_KEY = "#com.abeade.plugin.fcm.push.pushdialog.data"
        private const val SAVE_KEY = "#com.abeade.plugin.fcm.push.pushdialog.save"
        private const val MESSAGE_KEY = "#com.abeade.plugin.fcm.push.pushdialog.message"
        private const val DIMENSION_SERVICE_KEY = "#com.abeade.plugin.fcm.push.pushdialog"
        private const val TO_PROPERTY = "to"
        private const val DATA_PROPERTY = "data"
        private const val DATA_LABEL = "Data"
        private const val MESSAGE_LABEL = "Message"
    }

    init {
        init()
        title = "FCM push sender"
        setOKButtonText("Send")
        setOKButtonIcon(AllIcons.Actions.Upload)
    }

    val pushData: PushData?
        get() = data

    private var data: PushData? = null

    private lateinit var dataField: CustomEditorField
    private lateinit var dialog: PushDialog

    override fun getDimensionServiceKey(): String = DIMENSION_SERVICE_KEY

    override fun createCenterPanel(): JComponent {
        var edited = false
        var updatingData = false
        var updatingTemplate = false
        val settingsManager = SettingsManager(project)
        val firebaseIdFromSharedPreference = discoverFirebaseIdUsingStetho()
        val firebaseId = firebaseIdFromSharedPreference ?: propertiesComponent.getValue(FIREBASE_KEY).orEmpty()
        var currentData = propertiesComponent.getValue(DATA_KEY).orEmpty()
        val saveKey = propertiesComponent.getBoolean(SAVE_KEY)
        val messageKey = propertiesComponent.getBoolean(MESSAGE_KEY)
        val templates = listOf("[current data]") + settingsManager.templates.map { it.name }.toList()

        dialog = PushDialog().apply {

            fun setEdited() {
                if (!updatingData) {
                    updatingTemplate = true
                    if (templatesComboBox.itemCount > 0) templatesComboBox.selectedIndex = 0
                    edited = true
                    updatingTemplate = false
                }
            }

            firebaseIdField.text = firebaseId
            dataField = CustomEditorField(JsonLanguage.INSTANCE, project, currentData).apply {
                addDocumentListener(object : DocumentListener {
                    override fun documentChanged(event: com.intellij.openapi.editor.event.DocumentEvent) {
                        setEdited()
                    }
                })
            }
            panelData.layout = BorderLayout()
            panelData.add(dataField)
            rememberCheckBox.isSelected = saveKey
            if (templates.size > 1) {
                templatesComboBox.isVisible = true
                templateLabel.isVisible = true
                templates.forEach { templatesComboBox.addItem(it) }
            } else {
                templatesComboBox.isVisible = false
                templateLabel.isVisible = false
            }
            templatesComboBox.apply {
                addItemListener { event ->
                    if (event.stateChange == ItemEvent.SELECTED && !updatingTemplate) {
                        updatingData = true
                        if (selectedIndex - 1 in 0 until settingsManager.templates.size) {
                            if (edited) {
                                currentData = dataField.text
                            }
                            dataField.text = settingsManager.templates[selectedIndex - 1].data
                            messageRadioButton.isSelected = settingsManager.templates[selectedIndex - 1].isMessage
                            dataRadioButton.isSelected = !settingsManager.templates[selectedIndex - 1].isMessage
                            edited = false
                        } else {
                            dataField.text = currentData
                            edited = false
                        }
                        updatingData = false
                    }
                }
            }
            searchWithStethoButton.addActionListener { reloadFirebaseIdFromStetho() }
            searchWithStethoButton.isVisible = settingsManager.useStetho
            val radioGroup = ButtonGroup()
            radioGroup.add(dataRadioButton)
            radioGroup.add(messageRadioButton)
            dataRadioButton.addItemListener {
                dataLabel.text = DATA_LABEL
                setEdited()
            }
            messageRadioButton.addItemListener {
                dataLabel.text = MESSAGE_LABEL
                setEdited()
            }
            messageRadioButton.isSelected = messageKey
            dataRadioButton.isSelected = !messageKey
        }
        return dialog.panelMain
    }

    private fun discoverFirebaseIdUsingStetho(): String? {
        val settingsManager = SettingsManager(project)
        if (!settingsManager.useStetho) return null
        var result: StethoResult
        var process: String? = null
        var device: String? = null
        val preferenceFile = if (settingsManager.preferenceFile.isNotBlank()) settingsManager.preferenceFile else null
        val preferenceKey = settingsManager.preferenceKey
        do {
            result = try {
                val id = StethoPreferenceSearcher().getSharedPreference(preferenceFile, preferenceKey, device, process, settingsManager.adbPort)
                if (id.isNullOrEmpty()) {
                    showNotification("Shared preference $preferenceKey not found in process $process", NotificationType.ERROR)
                }
                StethoResult.Success(id)
            } catch (e: MultipleDevicesException) {
                StethoResult.MultipleDevicesError(e.devices)
            } catch (e: MultipleStethoProcessesException) {
                StethoResult.MultipleProcessError(e.processes)
            } catch (e: HumanReadableException) {
                showNotification(e.reason, NotificationType.ERROR)
                StethoResult.Error
            } catch (e: Exception) {
                showNotification(e.toString(), NotificationType.ERROR)
                StethoResult.Error
            }
            if (result is StethoResult.MultipleProcessError) {
                val dialog = StethoProcessDialogWrapper(result.processes)
                if (dialog.showAndGet()) {
                    process = dialog.selectedProcess
                } else {
                    result = StethoResult.Error
                }
            } else if (result is StethoResult.MultipleDevicesError) {
                val dialog = DeviceDialogWrapper(result.devices)
                if (dialog.showAndGet()) {
                    device = dialog.selectedDevice
                } else {
                    result = StethoResult.Error
                }
            }
        } while (result is StethoResult.MultipleProcessError || result is StethoResult.MultipleDevicesError)
        return when (result) {
            is StethoResult.Success -> result.value
            else -> null
        }
    }

    private fun reloadFirebaseIdFromStetho() {
        val old = dialog.firebaseIdField.text
        dialog.firebaseIdField.text = String.EMPTY
        discoverFirebaseIdUsingStetho()?.let {
            dialog.firebaseIdField.text = it
        } ?: run { dialog.firebaseIdField.text = old }
    }

    override fun doOKAction() {
        val isMessage = dialog.messageRadioButton.isSelected
        val dataJsonObject = JsonParser.parseString(dataField.text).asJsonObject
        val jsonString = if (isMessage) {
            dataJsonObject.add(TO_PROPERTY, JsonPrimitive(dialog.firebaseIdField.text))
            dataJsonObject.toString()
        } else {
            JsonObject().apply {
                add(TO_PROPERTY, JsonPrimitive(dialog.firebaseIdField.text))
                add(DATA_PROPERTY, dataJsonObject)
            }.toString()
        }
        data = PushData(jsonString)
        val remember = dialog.rememberCheckBox.isSelected
        propertiesComponent.setValue(FIREBASE_KEY, if (remember) dialog.firebaseIdField.text else null)
        propertiesComponent.setValue(DATA_KEY, if (remember) dataField.text else null)
        propertiesComponent.setValue(MESSAGE_KEY, if (remember) dialog.messageRadioButton.isSelected else false)
        propertiesComponent.setValue(SAVE_KEY, remember)
        super.doOKAction()
    }

    override fun doValidate(): ValidationInfo? = when {
        dialog.firebaseIdField.text.isBlank() -> ValidationInfo("Firebase Id required", dialog.firebaseIdField)
        dataField.text.isBlank() -> ValidationInfo("Data field required", dataField)
        !isValidJson() -> ValidationInfo("$dataFieldName must be a valid JSON object", dataField)
        dialog.messageRadioButton.isSelected && JsonParser.parseString(dataField.text).asJsonObject.has(TO_PROPERTY) ->
            ValidationInfo("$dataFieldName must not contain the \"to\" field", dataField)
        else -> null
    }

    private val dataFieldName
        get() = if (dialog.messageRadioButton.isSelected) MESSAGE_LABEL else DATA_LABEL

    private fun isValidJson() = try {
        JsonParser.parseString(dataField.text).isJsonObject
    } catch (e: JsonParseException) {
        false
    }

    sealed class StethoResult {
        data class Success(val value: String?): StethoResult()
        object Error: StethoResult()
        data class  MultipleProcessError(val processes: List<String>): StethoResult()
        data class  MultipleDevicesError(val devices: List<String>): StethoResult()
    }
}
