package com.abeade.plugin.fcm.push.ui

import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.JButton
import javax.swing.JCheckBox
import com.intellij.openapi.ui.ComboBox
import javax.swing.JLabel
import javax.swing.JRadioButton

class PushDialog {
    lateinit var panelMain: JPanel
    lateinit var firebaseIdField: JTextField
    lateinit var searchWithStethoButton: JButton
    lateinit var rememberCheckBox: JCheckBox
    lateinit var templatesComboBox: ComboBox<String>
    lateinit var panelData: JPanel
    lateinit var templateLabel: JLabel
    lateinit var dataLabel: JLabel
    lateinit var dataRadioButton: JRadioButton
    lateinit var messageRadioButton: JRadioButton
}