package com.thoughtapps.droppoint.core.messageExchange.highLevel;

import com.thoughtapps.droppoint.core.dto.Message;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by zaskanov on 05.04.2017.
 */
@Slf4j
public abstract class AbstractMessageProcessor implements MessageProcessor {

    @Override
    public Message processMessage(Message request) {
        try {
            log.info("Start {}", this.getClass().getSimpleName());

            Message message = processInternal(request);

            log.info("Finish {}", this.getClass().getSimpleName());

            return message;
        } catch (Exception e) {
            log.error("Error in {}: {}", this.getClass().getSimpleName(), e);
            return Message.ERROR;
        }
    }

    protected abstract Message processInternal(Message request) throws Exception;
}
