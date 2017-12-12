package com.thoughtapps.droppoint.droppoint.ui.validators;

import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.DefaultProperty;
import javafx.scene.control.TextInputControl;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.UrlValidator;

/**
 * Created by zaskanov on 23.04.2017.
 */

/**
 * Check if value is valid host name
 */
@DefaultProperty(value = "icon")
public class HostnameValidator extends ValidatorBase {

    private InetAddressValidator inetAddressValidator = InetAddressValidator.getInstance();

    private UrlValidator urlValidator = UrlValidator.getInstance();

    @Override
    protected void eval() {
        if (srcControl.get() instanceof TextInputControl) {
            evalTextInputField();
        }
    }

    private void evalTextInputField() {
        TextInputControl textField = (TextInputControl) srcControl.get();
        try {
            if (inetAddressValidator.isValid(textField.getText()) || urlValidator.isValid(textField.getText()))
                hasErrors.set(false);
            else hasErrors.set(true);
        } catch (Exception e) {
            hasErrors.set(true);
        }
    }
}
