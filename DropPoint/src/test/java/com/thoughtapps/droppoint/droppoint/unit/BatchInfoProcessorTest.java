package com.thoughtapps.droppoint.droppoint.unit;

import com.thoughtapps.droppoint.core.dto.*;
import com.thoughtapps.droppoint.core.helpers.CGSON;
import com.thoughtapps.droppoint.core.messageExchange.highLevel.MessageProcessor;
import com.thoughtapps.droppoint.droppoint.model.FileTransfer;
import com.thoughtapps.droppoint.droppoint.model.FileTransferBatch;
import com.thoughtapps.droppoint.droppoint.model.TransferStatus;
import com.thoughtapps.droppoint.droppoint.repositories.FileTransferBatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;

import static org.testng.Assert.*;

/**
 * Created by zaskanov on 01.04.2017.
 */
@Test
public class BatchInfoProcessorTest extends AbstractTransactionalTest {

    @Autowired
    MessageProcessor batchInfoProcessor;

    @Autowired
    FileTransferBatchRepository ftbRepository;

    @Test
    public void testBatchInfo() {
        FileTransferBatch ftb = FileTransferBatch.builder().deleteOriginal(true).status(TransferStatus.WAITING)
                .useCompression(true).fileTransfers(new ArrayList<>()).nodeId("123").build();
        FileTransfer ft = FileTransfer.builder().filePath("candidates.txt").fileTransferBatch(ftb).build();
        ftb.getFileTransfers().add(ft);
        ftb = ftbRepository.save(ftb);
        BatchesContainer container = BatchesContainer.builder().batches(new ArrayList<>()).build();
        Batch batch = Batch.builder().id(ftb.getId()).build();
        container.getBatches().add(batch);

        Message message = Message.builder().type(MessageType.BATCH_INFO).payloadJSON(CGSON.toJson(container)).build();
        Message response = batchInfoProcessor.processMessage(message);

        assertNotNull(response);
        assertEquals(response.getType(), MessageType.OK);

        Iterator<FileTransferBatch> batchIterator = ftbRepository.findAll().iterator();
        assertTrue(batchIterator.hasNext());

        BatchesContainer receivedBC = CGSON.fromJson(response.getPayloadJSON(), BatchesContainer.class);
        assertTrue(receivedBC.getBatches().size() == 1);

        Batch receivedB = receivedBC.getBatches().get(0);
        assertEquals(receivedB.getNodeId(), ftb.getNodeId());
        assertEquals(receivedB.getDeleteOriginal(), ftb.getDeleteOriginal());
        assertEquals(receivedB.getUseCompression(), ftb.getUseCompression());
        assertEquals(receivedB.getFiles().size(), 1);

        File file = receivedB.getFiles().get(0);
        assertEquals(file.getFilePath(), ft.getFilePath());
    }
}
