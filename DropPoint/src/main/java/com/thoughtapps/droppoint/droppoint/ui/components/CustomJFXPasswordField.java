package com.thoughtapps.droppoint.droppoint.ui.components;

import com.jfoenix.controls.JFXPasswordField;
import javafx.beans.property.ReadOnlyBooleanProperty;

/**
 * Created by zaskanov on 23.04.2017.
 */
public class CustomJFXPasswordField extends JFXPasswordField implements HasPropertyName, ValidationSupported {

    private String propertyName;

    @Override
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public ReadOnlyBooleanProperty getFocusedProperty() {
        return this.focusedProperty();
    }
}
