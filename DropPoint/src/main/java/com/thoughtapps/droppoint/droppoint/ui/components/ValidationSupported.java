package com.thoughtapps.droppoint.droppoint.ui.components;

import javafx.beans.property.ReadOnlyBooleanProperty;

/**
 * Created by zaskanov on 01.05.2017.
 */
public interface ValidationSupported {
    ReadOnlyBooleanProperty getFocusedProperty();

    boolean validate();
}
