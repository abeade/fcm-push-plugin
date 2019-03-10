package com.abeade.plugin.fcm.push;

import com.intellij.openapi.util.Comparing;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.text.NumberFormat;

public class PushSettingsPanel {

    private JPanel panel;
    private JTextField preferenceKeyField;
    private JTextField authorizationField;
    private JFormattedTextField adbPortField;
    private SettingsManager settingsManager;

    public JComponent createPanel() {
        this.settingsManager = new SettingsManager();
        return panel;
    }

    public boolean isModified() {
        return !Comparing.equal(preferenceKeyField.getText(), settingsManager.getPreferenceKey()) ||
                !Comparing.equal(authorizationField.getText(), settingsManager.getAuthorization()) ||
                !Comparing.equal(adbPortField.getText(), settingsManager.getAdbPort().toString());
    }

    public void apply() {
        settingsManager.setAuthorization(authorizationField.getText());
        settingsManager.setPreferenceKey(preferenceKeyField.getText());
        settingsManager.setAdbPort(Integer.parseInt(adbPortField.getText()));
    }

    public void reset() {
        preferenceKeyField.setText(settingsManager.getPreferenceKey());
        authorizationField.setText(settingsManager.getAuthorization());
        adbPortField.setText(settingsManager.getAdbPort().toString());
    }

    private void createUIComponents() {
        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(false);
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(0);
        formatter.setMaximum(Integer.MAX_VALUE);
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true);
        adbPortField = new JFormattedTextField(formatter);
    }
}
