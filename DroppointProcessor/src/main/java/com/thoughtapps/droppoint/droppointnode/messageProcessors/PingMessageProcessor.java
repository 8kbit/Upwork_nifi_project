package com.thoughtapps.droppoint.droppointnode.messageProcessors;

import com.thoughtapps.droppoint.core.dto.Message;
import com.thoughtapps.droppoint.core.dto.Ping;
import com.thoughtapps.droppoint.core.helpers.CGSON;
import com.thoughtapps.droppoint.core.messageExchange.highLevel.AbstractMessageProcessor;
import com.thoughtapps.droppoint.droppointnode.holders.DropPointInfo;
import com.thoughtapps.droppoint.droppointnode.holders.DropPointInfoHolder;
import com.thoughtapps.droppoint.droppointnode.holders.FetchQueueHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by zaskanov on 02.04.2017.
 */

/**
 * Process ping message
 */
@Slf4j
@Component
public class PingMessageProcessor extends AbstractMessageProcessor {

    @Autowired
    private DropPointInfoHolder dropPointInfoHolder;

    @Autowired
    private FetchQueueHolder fetchQueueHolder;

    @Override
    protected Message processInternal(Message request) throws Exception {
        Ping ping = CGSON.fromJson(request.getPayloadJSON(), Ping.class);

        fetchQueueHolder.addAll(ping.getWaitingBatches().getBatches());

        return Message.OK;
    }
}
