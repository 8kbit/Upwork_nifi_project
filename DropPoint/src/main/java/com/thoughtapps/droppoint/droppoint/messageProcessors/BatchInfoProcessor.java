package com.thoughtapps.droppoint.droppoint.messageProcessors;

import com.thoughtapps.droppoint.core.dto.Batch;
import com.thoughtapps.droppoint.core.dto.BatchesContainer;
import com.thoughtapps.droppoint.core.helpers.CGSON;
import com.thoughtapps.droppoint.core.messageExchange.highLevel.AbstractMessageProcessor;
import com.thoughtapps.droppoint.core.dto.Message;
import com.thoughtapps.droppoint.core.dto.MessageType;
import com.thoughtapps.droppoint.droppoint.converters.FileTransferBatchToBatchConverter;
import com.thoughtapps.droppoint.droppoint.model.FileTransferBatch;
import com.thoughtapps.droppoint.droppoint.repositories.FileTransferBatchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zaskanov on 05.04.2017.
 */
@Slf4j
@Component
public class BatchInfoProcessor extends AbstractMessageProcessor {

    @Autowired
    FileTransferBatchRepository ftbRepository;

    @Autowired
    FileTransferBatchToBatchConverter converter;

    /**
     * Process incoming request from drop point processor
     * @param request - request containing batch id
     * @return response with detailed info about batch and contained files
     */
    @Transactional(readOnly = true)
    @Override
    public Message processInternal(Message request) {
        BatchesContainer container = CGSON.fromJson(request.getPayloadJSON(), BatchesContainer.class);

        List<Batch> newBatches = new ArrayList<>();
        for (Batch originalBatch : container.getBatches()) {
            FileTransferBatch ftb = ftbRepository.findOne(originalBatch.getId());
            newBatches.add(converter.convert(ftb, false));
        }
        container.setBatches(newBatches);

        return Message.builder().type(MessageType.OK).payloadJSON(CGSON.toJson(container)).build();
    }
}
