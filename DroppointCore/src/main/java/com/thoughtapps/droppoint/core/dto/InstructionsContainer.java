package com.thoughtapps.droppoint.core.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * Created by zaskanov on 01.04.2017.
 */

/**
 * Container for instructions
 */
@Data
public class InstructionsContainer {
    @SerializedName("sftp.client.identity.id")
    String nodeId;
    @SerializedName("sftp.server.heartbeat.interval")
    Integer heartbeatIntervalSec;
    @SerializedName("sftp.server.instructions")
    List<Instruction> instructions;
}
