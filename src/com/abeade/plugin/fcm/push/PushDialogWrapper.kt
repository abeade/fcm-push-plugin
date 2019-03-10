package com.abeade.plugin.fcm.push

import com.abeade.plugin.fcm.push.SettingsManager.Companion.DEFAULT_PREFERENCE
import com.abeade.plugin.fcm.push.SettingsManager.Companion.PREFERENCE_KEY
import com.abeade.plugin.fcm.push.stetho.HumanReadableException
import com.abeade.plugin.fcm.push.stetho.MultipleStethoProcessesException
import com.abeade.plugin.fcm.push.stetho.StethoPreferenceSearcher
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.intellij.icons.AllIcons
import com.intellij.ide.util.PropertiesComponent
import com.intellij.json.JsonLanguage
import com.intellij.lang.Language
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.LanguageTextField
import com.intellij.ui.layout.panel
import java.awt.Dimension
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JTextField


class PushDialogWrapper(private val propertiesComponent: PropertiesComponent) : DialogWrapper(true) {

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
        dataField = CustomEditorField(JsonLanguage.INSTANCE, null, data).apply {
            setOneLineMode(false)
        }
        rememberCheckBox = JCheckBox().apply { isSelected = saveKey }

        return panel {
            row("Firebase ID") {
                firebaseIdField(pushX)
                button("Search with Stetho") { reloadFirebaseIdFromStetho() }
            }
            row {
                cell { JLabel("Data").apply { verticalAlignment = JLabel.TOP }(push, grow) }
                cell { dataField(grow, grow) }
            }
            row("Remember") { rememberCheckBox() }
        }.apply { minimumSize = Dimension(600, 200) }
    }

    class CustomEditorField(language: Language, project: Project?, s: String) : LanguageTextField(language, project, s) {

        override fun createEditor(): EditorEx {
            val editor = super.createEditor()
            editor.setVerticalScrollbarVisible(true)
            editor.setHorizontalScrollbarVisible(true)

            val settings = editor.settings
            settings.isLineNumbersShown = true
            settings.isAutoCodeFoldingEnabled = true
            settings.isFoldingOutlineShown = true
            settings.isAllowSingleLogicalLineFolding = true
            settings.isRightMarginShown=true
            settings.setTabSize(4)
            return editor
        }
    }

    private fun discoverFirebaseIdUsingStetho(): String? {
        var result: StethoResult
        var process: String? = null
        do {
            val preferenceKey = propertiesComponent.getValue(PREFERENCE_KEY) ?: DEFAULT_PREFERENCE
            result = try {
                StethoResult.Success(StethoPreferenceSearcher().getSharedPreference(preferenceKey, process,
                    SettingsManager().adbPort))
            } catch (e: MultipleStethoProcessesException) {
                showNotification(e.reason, true)
                StethoResult.MultipleProcessError(e.processes)
            } catch (e: HumanReadableException) {
                showNotification(e.reason, true)
                StethoResult.Error
            } catch (e: Exception) {
                showNotification(e.toString(), true)
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
        data = PushData(firebaseIdField.text, JsonParser().parse(dataField.text).toString())
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
