package com.thoughtapps.droppoint.droppoint.unit;

import com.thoughtapps.droppoint.core.dto.Batch;
import com.thoughtapps.droppoint.core.dto.BatchesContainer;
import com.thoughtapps.droppoint.core.dto.Message;
import com.thoughtapps.droppoint.core.dto.MessageType;
import com.thoughtapps.droppoint.core.helpers.CGSON;
import com.thoughtapps.droppoint.core.messageExchange.highLevel.MessageProcessor;
import com.thoughtapps.droppoint.droppoint.model.FileTransfer;
import com.thoughtapps.droppoint.droppoint.model.FileTransferBatch;
import com.thoughtapps.droppoint.droppoint.model.TransferStatus;
import com.thoughtapps.droppoint.droppoint.repositories.FileTransferBatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Created by zaskanov on 05.04.2017.
 */
@Test
public class MarkBatchReceivedProcessorTest extends AbstractTransactionalTest {

    @Autowired
    MessageProcessor markBatchReceivedProcessor;

    @Autowired
    FileTransferBatchRepository ftbRepository;

    @Test
    public void testBatchInfo() {
        FileTransferBatch ftb = FileTransferBatch.builder().useCompression(false).deleteOriginal(true).status(TransferStatus.WAITING)
                .fileTransfers(new ArrayList<>()).nodeId("123").build();
        FileTransfer ft = FileTransfer.builder().filePath("candidates.txt").fileTransferBatch(ftb).build();
        ftb.getFileTransfers().add(ft);
        ftb = ftbRepository.save(ftb);
        BatchesContainer container = BatchesContainer.builder().batches(new ArrayList<>()).build();
        Batch batch = Batch.builder().id(ftb.getId()).build();
        container.getBatches().add(batch);

        Message message = Message.builder().type(MessageType.BATCH_RECEIVED).payloadJSON(CGSON.toJson(container)).build();
        Message response = markBatchReceivedProcessor.processMessage(message);

        assertNotNull(response);
        assertEquals(response.getType(), MessageType.OK);

        FileTransferBatch reloadedFtb = ftbRepository.findOne(ftb.getId());
        assertEquals(reloadedFtb.getStatus(), TransferStatus.TRANSFERRED);
    }
}