package com.abeade.plugin.fcm.push.utils

import com.intellij.lang.Language
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.ui.LanguageTextField

class CustomEditorField(language: Language, project: Project?, s: String) : LanguageTextField(language, project, s) {

    override fun createEditor(): EditorEx {
        val editor = super.createEditor()
        editor.isOneLineMode = false
        editor.setVerticalScrollbarVisible(true)
        editor.setHorizontalScrollbarVisible(true)

        val settings = editor.settings
        settings.isLineNumbersShown = true
        settings.isAutoCodeFoldingEnabled = true
        settings.isFoldingOutlineShown = true
        settings.isRightMarginShown = true
        settings.isUseCustomSoftWrapIndent = true
        settings.customSoftWrapIndent = 4
        settings.isIndentGuidesShown = true
        settings.isShowIntentionBulb = true
        settings.setTabSize(4)
        return editor
    }
}
