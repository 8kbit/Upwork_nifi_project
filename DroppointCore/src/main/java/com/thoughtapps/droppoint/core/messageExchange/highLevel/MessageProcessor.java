package com.thoughtapps.droppoint.core.messageExchange.highLevel;

import com.thoughtapps.droppoint.core.dto.Message;

/**
 * Created by zaskanov on 02.04.2017.
 */

/**
 * used to process message received through ssh connection (ping, instructions etc)
 */
public interface MessageProcessor {
    public Message processMessage(Message request);
}
