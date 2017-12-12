package com.thoughtapps.droppoint.droppoint.ui.validators;

import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.DefaultProperty;
import javafx.scene.control.TextInputControl;
import org.apache.commons.validator.routines.IntegerValidator;

/**
 * Created by zaskanov on 23.04.2017.
 */

/**
 * Check if value is a valid port number
 */
@DefaultProperty(value = "icon")
public class PortValidator extends ValidatorBase {

    private IntegerValidator validator = new IntegerValidator();

    @Override
    protected void eval() {
        if (srcControl.get() instanceof TextInputControl) {
            evalTextInputField();
        }
    }

    private void evalTextInputField() {
        TextInputControl textField = (TextInputControl) srcControl.get();
        try {
            Integer port = validator.validate(textField.getText());
            if (port != null && validator.isInRange(port, 0, 65536)) hasErrors.set(false);
            else hasErrors.set(true);
        } catch (Exception e) {
            hasErrors.set(true);
        }
    }
}