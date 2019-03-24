package com.abeade.plugin.fcm.push.utils

import com.intellij.lang.Language
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.ui.LanguageTextField

class CustomEditorField(language: Language, project: Project, s: String) : LanguageTextField(language, project, s) {

    override fun createEditor(): EditorEx =
        super.createEditor().apply {
            isOneLineMode = false
            setVerticalScrollbarVisible(true)
            setHorizontalScrollbarVisible(true)
            settings.apply {
                isLineNumbersShown = true
                isAutoCodeFoldingEnabled = true
                isFoldingOutlineShown = true
                isRightMarginShown = true
                isUseCustomSoftWrapIndent = true
                customSoftWrapIndent = 4
                isIndentGuidesShown = true
                isShowIntentionBulb = true
                setTabSize(4)
            }
        }
}
