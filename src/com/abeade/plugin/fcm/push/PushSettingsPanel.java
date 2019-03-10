package com.abeade.plugin.fcm.push;

import com.intellij.openapi.util.Comparing;

import javax.swing.*;

public class PushSettingsPanel {

    private JPanel panel;
    private JTextField preferenceKeyField;
    private JTextField authorizationField;
    private SettingsManager settingsManager;

    public JComponent createPanel() {
        this.settingsManager = new SettingsManager();
        return panel;
    }

    public boolean isModified() {
        return !Comparing.equal(preferenceKeyField.getText(), settingsManager.getPreferenceKey()) ||
                !Comparing.equal(authorizationField.getText(), settingsManager.getAuthorization());
    }

    public void apply() {
        settingsManager.setAuthorization(authorizationField.getText());
        settingsManager.setPreferenceKey(preferenceKeyField.getText());
    }

    public void reset() {
        preferenceKeyField.setText(settingsManager.getPreferenceKey());
        authorizationField.setText(settingsManager.getAuthorization());
    }
}
