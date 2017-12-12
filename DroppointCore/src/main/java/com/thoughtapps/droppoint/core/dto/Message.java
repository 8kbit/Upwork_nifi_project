package com.thoughtapps.droppoint.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Created by zaskanov on 02.04.2017.
 */

/**
 * Container for message (communication between drop point processor and drop point)
 */
@Data
@AllArgsConstructor
@Builder
public class Message {

    public static final Message UNKNOWN_COMMAND = new Message(MessageType.UNKNOWN_COMMAND, null);
    public static final Message OK = new Message(MessageType.OK, null);
    public static final Message ERROR = new Message(MessageType.ERROR, null);

    MessageType type;
    String payloadJSON;
}
