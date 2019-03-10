package com.abeade.plugin.fcm.push;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.util.Comparing;

import javax.swing.*;

import static com.intellij.credentialStore.CredentialAttributesKt.generateServiceName;

public class PushSettingsPanel {

    private JPanel panel;
    private JTextField preferenceKeyField;
    private JTextField authorizationField;
    private PropertiesComponent propertiesComponent;
    private CredentialAttributes credentialAttributes;

    public JComponent createPanel() {
        this.propertiesComponent = PropertiesComponent.getInstance();
        this.credentialAttributes = new CredentialAttributes(generateServiceName("PushSystem", PushDialogWrapper.AUTHORIZATION_KEY));
        return panel;
    }

    public boolean isModified() {
        return !Comparing.equal(preferenceKeyField.getText(), getCurrentPreferenceKey()) ||
                !Comparing.equal(authorizationField.getText(), getCurrentAuthorization());
    }

    public void apply() {
        propertiesComponent.setValue(PushDialogWrapper.PREFERENCE_KEY, preferenceKeyField.getText());
        Credentials credentials = new Credentials("", authorizationField.getText());
        PasswordSafe.getInstance().set(credentialAttributes, credentials);
    }

    public void reset() {
        preferenceKeyField.setText(getCurrentPreferenceKey());
        authorizationField.setText(getCurrentAuthorization());
    }

    private String getCurrentPreferenceKey() {
        String preferenceKey = propertiesComponent.getValue(PushDialogWrapper.PREFERENCE_KEY);
        if (preferenceKey == null) {
            preferenceKey = PushDialogWrapper.DEFAULT_PREFERENCE;
        }
        return preferenceKey;
    }

    private String getCurrentAuthorization() {
        return PasswordSafe.getInstance().getPassword(credentialAttributes);
    }
}
