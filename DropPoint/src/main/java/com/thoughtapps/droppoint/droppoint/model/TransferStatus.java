package com.thoughtapps.droppoint.droppoint.model;

/**
 * Created by zaskanov on 04.04.2017.
 */

/**
 * information about if drop point processor processed this batch
 */
public enum TransferStatus {
    WAITING("WAITING"),
    TRANSFERRED("TRANSFERRED");

    private String value;

    TransferStatus(String value) {
        this.value = value;
    }
}
