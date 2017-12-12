package com.thoughtapps.droppoint.droppoint.messageProcessors;

import com.thoughtapps.droppoint.core.dto.Batch;
import com.thoughtapps.droppoint.core.dto.BatchesContainer;
import com.thoughtapps.droppoint.core.helpers.CGSON;
import com.thoughtapps.droppoint.core.messageExchange.highLevel.AbstractMessageProcessor;
import com.thoughtapps.droppoint.core.dto.Message;
import com.thoughtapps.droppoint.droppoint.model.FileTransferBatch;
import com.thoughtapps.droppoint.droppoint.model.TransferStatus;
import com.thoughtapps.droppoint.droppoint.repositories.FileTransferBatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by zaskanov on 05.04.2017.
 */
@Component
public class MarkBatchReceivedProcessor extends AbstractMessageProcessor {

    @Autowired
    FileTransferBatchRepository ftbRepository;

    /**
     * Mark batch as received (means that drop point processor processed this batch)
     * @param request containing batch ids
     * @return standard OK response
     */
    @Transactional
    @Override
    public Message processInternal(Message request) {
        BatchesContainer container = CGSON.fromJson(request.getPayloadJSON(), BatchesContainer.class);

        for (Batch batch : container.getBatches()) {
            FileTransferBatch ftb = ftbRepository.findOne(batch.getId());
            ftb.setStatus(TransferStatus.TRANSFERRED);
            ftbRepository.save(ftb);
        }

        return Message.OK;
    }
}
