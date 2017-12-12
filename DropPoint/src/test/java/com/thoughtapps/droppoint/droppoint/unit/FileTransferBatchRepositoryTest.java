package com.thoughtapps.droppoint.droppoint.unit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.Test;
import com.thoughtapps.droppoint.droppoint.model.FileTransfer;
import com.thoughtapps.droppoint.droppoint.model.FileTransferBatch;
import com.thoughtapps.droppoint.droppoint.model.TransferStatus;
import com.thoughtapps.droppoint.droppoint.repositories.FileTransferBatchRepository;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by zaskanov on 01.04.2017.
 */
@Test
public class FileTransferBatchRepositoryTest extends AbstractTransactionalTest {

    @Autowired
    FileTransferBatchRepository ftbRepository;

    @Test
    public void testSave() {
        FileTransferBatch ftb = FileTransferBatch.builder().status(TransferStatus.WAITING).useCompression(false)
                .deleteOriginal(false).fileTransfers(new ArrayList<>()).nodeId("123").build();
        FileTransfer transfer = FileTransfer.builder().fileTransferBatch(ftb).filePath("C://candidates.txt").build();
        ftb.getFileTransfers().add(transfer);
        ftb = ftbRepository.save(ftb);

        FileTransferBatch ftb2 = ftbRepository.findOne(ftb.getId());
        assertTrue(ftb2.getFileTransfers().size() == 1);
        assertNotNull(ftb2.getCreatedDate());
    }

    @Test
    public void testFindPendingBatches() {
        FileTransferBatch ftb1 = FileTransferBatch.builder().deleteOriginal(false).useCompression(false)
                .status(TransferStatus.WAITING).nodeId("123").build();
        FileTransferBatch ftb2 = FileTransferBatch.builder().deleteOriginal(false).useCompression(false)
                .status(TransferStatus.WAITING).nodeId("123").build();
        FileTransferBatch ftb3 = FileTransferBatch.builder().deleteOriginal(false).useCompression(false)
                .status(TransferStatus.TRANSFERRED).nodeId("123").build();

        ftb1 = ftbRepository.save(ftb1);
        ftb2 = ftbRepository.save(ftb2);
        ftb3 = ftbRepository.save(ftb3);

        List<FileTransferBatch> batchList = ftbRepository.findByStatusOrderByCreatedDateAsc(TransferStatus.WAITING);
        assertEquals(batchList.size(), 2);
        assertEquals(batchList.get(0).getId(), ftb1.getId());
        assertEquals(batchList.get(1).getId(), ftb2.getId());
    }
}
