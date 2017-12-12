package com.thoughtapps.droppoint.core.dto;

/**
 * Created by zaskanov on 02.04.2017.
 */
public enum MessageType {
    PING, // pings processor
    INSTRUCTIONS, // sends instructions to drop point
    OK, //indicates that request successfully processed
    BATCH_INFO, //request for full information about batch
    BATCH_RECEIVED, //mark batch as processed by drop point processor
    ERROR, //error while message processing
    UNKNOWN_COMMAND, // if server can not process message due to not supported message type
    PORT_REQUEST // request for assigned remote port for forwarding
}
