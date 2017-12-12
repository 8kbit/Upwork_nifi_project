package com.thoughtapps.droppoint.droppointnode.messageProcessors;

import com.thoughtapps.droppoint.core.dto.Message;
import com.thoughtapps.droppoint.core.dto.MessageType;
import com.thoughtapps.droppoint.core.helpers.CGSON;
import com.thoughtapps.droppoint.core.messageExchange.highLevel.AbstractMessageProcessor;
import com.thoughtapps.droppoint.droppointnode.Configuration;
import com.thoughtapps.droppoint.droppointnode.holders.DropPointInfoHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by zaskanov on 02.04.2017.
 */

/**
 * Process request from drop point. Drop point asking about port assigned to it
 */
@Slf4j
@Component
public class PortRequestMessageProcessor extends AbstractMessageProcessor {

    @Autowired
    private DropPointInfoHolder dropPointInfoHolder;

    @Autowired
    private Configuration configuration;

    @Override
    protected Message processInternal(Message request) throws Exception {
        String dropPointId = CGSON.fromJson(request.getPayloadJSON(), String.class);

        if (!configuration.getDropPointIds().contains(dropPointId)) return Message.ERROR;

        Integer port = dropPointInfoHolder.getDropPointPort(dropPointId);

        if (port == null) {
            log.error("Port not found for client: {}", dropPointId);
            return Message.ERROR;
        }

        return Message.builder().type(MessageType.OK).payloadJSON(CGSON.toJson(port)).build();
    }
}
