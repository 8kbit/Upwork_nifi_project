package com.thoughtapps.droppoint.droppoint.unit;

import com.thoughtapps.droppoint.core.dto.*;
import com.thoughtapps.droppoint.core.helpers.CGSON;
import com.thoughtapps.droppoint.core.messageExchange.highLevel.MessageProcessor;
import com.thoughtapps.droppoint.droppoint.model.FileTransferBatch;
import com.thoughtapps.droppoint.droppoint.model.Task;
import com.thoughtapps.droppoint.droppoint.repositories.FileTransferBatchRepository;
import com.thoughtapps.droppoint.droppoint.repositories.TaskRepository;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Iterator;

import static org.testng.Assert.*;

/**
 * Created by zaskanov on 01.04.2017.
 */
@Test
public class InstructionsProcessorTest extends AbstractTransactionalTest {

    @Autowired
    MessageProcessor instructionsProcessor;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    FileTransferBatchRepository ftbRepository;

    public void testPullProcessing() throws IOException {
        String payload = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("PullInstruction.json"));
        InstructionsContainer instructions = CGSON.fromJson(payload, InstructionsContainer.class);
        instructions.setNodeId("123");
        Message instrMessage = Message.builder().type(MessageType.INSTRUCTIONS).payloadJSON(CGSON.toJson(instructions))
                .build();

        Message response = instructionsProcessor.processMessage(instrMessage);
        Iterator<Task> taskIt = taskRepository.findAll().iterator();

        assertTrue(taskIt.hasNext());

        Task task = taskRepository.findOne(taskIt.next().getId());
        assertNotNull(task);
        assertEquals(task.getType(), InstructionType.PULL_FILE);
        assertEquals(task.getInstruction(), instructions.getInstructions().get(0));

        assertNotNull(response);
        assertEquals(response.getType(), MessageType.OK);

        BatchesContainer container = CGSON.fromJson(response.getPayloadJSON(), BatchesContainer.class);
        assertEquals(container.getBatches().size(), 1);

        Long batchId = container.getBatches().get(0).getId();
        FileTransferBatch ftb = ftbRepository.findOne(batchId);
        assertNotNull(ftb);
        assertEquals(ftb.getNodeId(), instructions.getNodeId());
        assertTrue(ftb.getFileTransfers().size() == 1);
        assertTrue(ftb.getFileTransfers().get(0).getFilePath().endsWith("example.txt"));
    }

}
