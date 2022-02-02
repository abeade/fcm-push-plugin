package com.abeade.plugin.fcm.push.ui

import javax.swing.JFormattedTextField
import javax.swing.JTextField
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JButton
import javax.swing.JSplitPane
import javax.swing.JRadioButton
import javax.swing.JLabel

class SettingsPanel {
    lateinit var adbPortField: JFormattedTextField
    lateinit var authorizationField: JTextField
    lateinit var useStethoField: JCheckBox
    lateinit var preferenceKeyField: JTextField
    lateinit var preferenceFileField: JTextField
    lateinit var templatesPanel: JPanel
    lateinit var exportButton: JButton
    lateinit var importButton: JButton
    lateinit var templateNameField: JTextField
    lateinit var templateDataPanel: JPanel
    lateinit var mainPanel: JPanel
    lateinit var generalSettingsPanel: JPanel
    lateinit var sethetoSettingsPanel: JPanel
    lateinit var templatesContainerPanel: JPanel
    lateinit var templateContentPanel: JPanel
    lateinit var templatesSplitPanel: JSplitPane
    lateinit var preferenceKeyHelpPanel: JPanel
    lateinit var preferenceFileHelpPanel: JPanel
    lateinit var templateDataRadioButton: JRadioButton
    lateinit var templateMessageRadioButton: JRadioButton
    lateinit var dataLabel: JLabel
}