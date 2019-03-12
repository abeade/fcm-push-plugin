package com.abeade.plugin.fcm.push

import com.abeade.plugin.fcm.push.model.PushData
import com.abeade.plugin.fcm.push.settings.DEFAULT_PREFERENCE_KEY
import com.abeade.plugin.fcm.push.settings.SettingsManager
import com.abeade.plugin.fcm.push.stetho.HumanReadableException
import com.abeade.plugin.fcm.push.stetho.MultipleStethoProcessesException
import com.abeade.plugin.fcm.push.stetho.StethoPreferenceSearcher
import com.abeade.plugin.fcm.push.utils.CustomEditorField
import com.abeade.plugin.fcm.push.utils.EMPTY
import com.abeade.plugin.fcm.push.utils.showNotification
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.intellij.icons.AllIcons
import com.intellij.ide.util.PropertiesComponent
import com.intellij.json.JsonLanguage
import com.intellij.lang.Language
import com.intellij.notification.NotificationType
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.LanguageTextField
import com.intellij.ui.layout.panel
import java.awt.Dimension
import javax.swing.*


class PushDialogWrapper(
    private val propertiesComponent: PropertiesComponent,
    private val project: Project
) : DialogWrapper(true) {

    companion object {

        private const val FIREBASE_KEY = "#com.abeade.plugin.fcm.push.pushdialog.firebase"
        private const val DATA_KEY = "#com.abeade.plugin.fcm.push.pushdialog.data"
        private const val SAVE_KEY = "#com.abeade.plugin.fcm.push.pushdialog.save"
        private const val DIMENSION_SERVICE_KEY = "#com.abeade.plugin.fcm.push.pushdialog"
    }

    init {
        init()
        title = "FCM push sender"
        setOKButtonText("Send")
        setOKButtonIcon(AllIcons.Actions.Upload)
        setCancelButtonIcon(AllIcons.Actions.Cancel)
    }

    val pushData: PushData?
        get() = data

    private var data: PushData? = null

    private lateinit var firebaseIdField: JTextField
    private lateinit var dataField: CustomEditorField
    private lateinit var rememberCheckBox: JCheckBox

    override fun getDimensionServiceKey(): String? = DIMENSION_SERVICE_KEY

    override fun createCenterPanel(): JComponent {
        val firebaseIdFromSharedPreference = discoverFirebaseIdUsingStetho()
        val firebaseId = firebaseIdFromSharedPreference ?: propertiesComponent.getValue(PushDialogWrapper.FIREBASE_KEY).orEmpty()
        val data = propertiesComponent.getValue(PushDialogWrapper.DATA_KEY).orEmpty()
        val saveKey = propertiesComponent.getBoolean(SAVE_KEY)
        firebaseIdField = JTextField(firebaseId)
        dataField = CustomEditorField(JsonLanguage.INSTANCE, project, data)
        rememberCheckBox = JCheckBox().apply { isSelected = saveKey }

        return panel {
            row("Firebase ID") {
                firebaseIdField(pushX)
                button("Search with Stetho") { reloadFirebaseIdFromStetho() }
            }
            //row("Template") { ComboBox<String>(arrayOf("", "one", "two"))(pushX) }
            row {
                cell { JLabel("Data").apply { verticalAlignment = JLabel.TOP }(push, grow) }
                cell { dataField(grow, grow) }
            }
            row("Remember") { rememberCheckBox() }
        }.apply { minimumSize = Dimension(600, 200) }
    }

    private fun discoverFirebaseIdUsingStetho(): String? {
        val settingsManager = SettingsManager()
        var result: StethoResult
        var process: String? = null
        do {
            val preferenceKey = settingsManager.preferenceKey ?: DEFAULT_PREFERENCE_KEY
            result = try {
                StethoResult.Success(StethoPreferenceSearcher().getSharedPreference(preferenceKey, process,
                    settingsManager.adbPort))
            } catch (e: MultipleStethoProcessesException) {
                showNotification(e.reason, NotificationType.WARNING)
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
            }
        } while (result is StethoResult.MultipleProcessError)
        return when (result) {
            is StethoResult.Success -> result.value
            else -> null
        }
    }

    private fun reloadFirebaseIdFromStetho() {
        val old = firebaseIdField.text
        firebaseIdField.text = String.EMPTY
        discoverFirebaseIdUsingStetho()?.let {
            firebaseIdField.text = it
        } ?: run { firebaseIdField.text = old }
    }

    override fun doOKAction() {
        data = PushData(
            firebaseIdField.text,
            JsonParser().parse(dataField.text).toString()
        )
        val remember = rememberCheckBox.isSelected
        propertiesComponent.setValue(FIREBASE_KEY, if (remember) firebaseIdField.text else null)
        propertiesComponent.setValue(DATA_KEY, if (remember) dataField.text else null)
        propertiesComponent.setValue(SAVE_KEY, remember)
        super.doOKAction()
    }

    override fun doValidate(): ValidationInfo? {
        return when {
            firebaseIdField.text.isBlank() -> ValidationInfo("Firebase Id required", firebaseIdField)
            dataField.text.isBlank() -> ValidationInfo("Data field required", dataField)
            !isValidJson() -> ValidationInfo("Data should be a valid JSON", dataField)
            else -> null
        }
    }

    private fun isValidJson() = try {
        JsonParser().parse(dataField.text)
        true
    } catch (e: JsonParseException) {
        false
    }

    sealed class StethoResult {
        data class Success(val value: String?): StethoResult()
        object Error: StethoResult()
        data class  MultipleProcessError(val processes: List<String>): StethoResult()
    }
}
