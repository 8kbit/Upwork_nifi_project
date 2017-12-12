package com.thoughtapps.droppoint.droppoint.ui.validators;

import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.DefaultProperty;
import javafx.scene.control.TextInputControl;
import org.apache.commons.lang.StringUtils;

import java.io.File;

/**
 * Created by zaskanov on 23.04.2017.
 */

/**
 * Check if folder exists
 */
@DefaultProperty(value = "icon")
public class FolderExistsValidator extends ValidatorBase {
    @Override
    protected void eval() {
        if (srcControl.get() instanceof TextInputControl) {
            evalTextInputField();
        }
    }

    private void evalTextInputField() {
        TextInputControl textField = (TextInputControl) srcControl.get();
        try {
            File file = new File(textField.getText());

            if (StringUtils.isNotBlank(textField.getText()) && file.exists() && file.isDirectory())
                hasErrors.set(false);
            else hasErrors.set(true);
        } catch (Exception e) {
            hasErrors.set(true);
        }
    }
}
