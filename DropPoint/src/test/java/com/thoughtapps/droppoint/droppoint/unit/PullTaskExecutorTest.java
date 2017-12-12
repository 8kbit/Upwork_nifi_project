package com.thoughtapps.droppoint.droppoint.unit;

import com.thoughtapps.droppoint.core.dto.Instruction;
import com.thoughtapps.droppoint.core.dto.InstructionType;
import com.thoughtapps.droppoint.droppoint.model.FileTransferBatch;
import com.thoughtapps.droppoint.droppoint.model.Task;
import com.thoughtapps.droppoint.droppoint.repositories.FileTransferBatchRepository;
import com.thoughtapps.droppoint.droppoint.repositories.TaskRepository;
import com.thoughtapps.droppoint.droppoint.task.TaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * Created by zaskanov on 01.04.2017.
 */
@Test
public class PullTaskExecutorTest extends AbstractTransactionalTest {

    @Qualifier("pullTaskExecutor")
    @Autowired
    TaskExecutor pullTaskExecutor;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    FileTransferBatchRepository ftbRepository;

    @Test
    public void testBatchInfo() throws URISyntaxException {
        Instruction instruction = new Instruction();
        instruction.setFilterIsIgnoreProcessedFile(true);
        instruction.setIsPushRecursively(true);
        instruction.setFilterRecursionDepth(10);
        instruction.setFilterFileType("txt");
        instruction.setFilterFilename("example.txt");
        Task task = Task.builder().type(InstructionType.PULL_FILE).nodeId("123").build();
        task.setInstruction(instruction);
        taskRepository.save(task);

        List<Long> batchIds = pullTaskExecutor.execute(task);
        assertEquals(batchIds.size(), 1);

        FileTransferBatch ftb = ftbRepository.findOne(batchIds.get(0));
        assertEquals(ftb.getFileTransfers().size(), 1);
        assertEquals(ftb.getNodeId(), task.getNodeId());
        assertEquals(Paths.get(ftb.getFileTransfers().get(0).getFilePath()).toString(),
                Paths.get("1level", "2level", "3level", "example.txt").toString());
    }
}
