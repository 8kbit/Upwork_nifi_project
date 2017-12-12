package com.thoughtapps.droppoint.droppoint.unit;

import com.thoughtapps.droppoint.droppoint.model.FileTransfer;
import com.thoughtapps.droppoint.droppoint.model.FileTransferBatch;
import com.thoughtapps.droppoint.droppoint.model.TransferStatus;
import com.thoughtapps.droppoint.droppoint.repositories.FileTransferBatchRepository;
import com.thoughtapps.droppoint.droppoint.service.FileTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * Created by zaskanov on 07.04.2017.
 */
@Test
public class FileTransferServiceTest extends AbstractTransactionalTest {
    @Autowired
    FileTransferService ftService;

    @Autowired
    FileTransferBatchRepository repository;

    @Test
    public void testFileFilter() {
        FileTransferBatch fileTransferBatch = FileTransferBatch.builder().status(TransferStatus.WAITING)
                .useCompression(false).deleteOriginal(true).fileTransfers(new ArrayList<>()).nodeId("123").build();
        FileTransfer transfer = FileTransfer.builder().filePath("C://candidates.txt").fileTransferBatch(fileTransferBatch).build();
        fileTransferBatch.getFileTransfers().add(transfer);
        repository.save(fileTransferBatch);

        List<String> paths = new ArrayList<>(Arrays.asList("C://candidates.txt", "C://2.txt"));
        List<String> filteredPaths = ftService.filterTransferredFiles(paths, fileTransferBatch.getNodeId());

        assertEquals(filteredPaths, new HashSet<>(Arrays.asList("C://2.txt")));
    }
}
