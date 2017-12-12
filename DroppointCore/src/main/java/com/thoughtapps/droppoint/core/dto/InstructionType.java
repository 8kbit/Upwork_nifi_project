package com.thoughtapps.droppoint.core.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zaskanov on 01.04.2017.
 */
public enum InstructionType {
    @SerializedName("pullfile")
    PULL_FILE,

    @SerializedName("pushfile")
    PUSH_FILE,

    @SerializedName("sql")
    SQL;
}
