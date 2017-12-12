package com.thoughtapps.droppoint.droppoint.unit;

import com.thoughtapps.droppoint.core.dto.Batch;
import com.thoughtapps.droppoint.core.dto.Message;
import com.thoughtapps.droppoint.core.dto.Ping;
import com.thoughtapps.droppoint.core.helpers.CGSON;
import com.thoughtapps.droppoint.core.messageExchange.highLevel.SshClientImpl;
import com.thoughtapps.droppoint.droppoint.model.FileTransfer;
import com.thoughtapps.droppoint.droppoint.model.FileTransferBatch;
import com.thoughtapps.droppoint.droppoint.model.TransferStatus;
import com.thoughtapps.droppoint.droppoint.repositories.FileTransferBatchRepository;
import com.thoughtapps.droppoint.droppoint.schedule.PingerJob;
import com.thoughtapps.droppoint.droppoint.schedule.SshConnectionHolder;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * Created by zaskanov on 05.04.2017.
 */
@Test
public class PingerJobTest extends AbstractTransactionalTest {

    @Autowired
    PingerJob pinger;

    @Autowired
    SshConnectionHolder sshConnectionHolder;

    @Autowired
    FileTransferBatchRepository ftbRepository;

    @Transactional
    @Test
    public void testRun() throws Exception {
        ArgumentCaptor<Message> argumentCaptor = ArgumentCaptor.forClass(Message.class);
        SshClientImpl sshClient = mock(SshClientImpl.class);
        when(sshClient.sendMessage(argumentCaptor.capture())).thenReturn(Message.OK);
        when(sshConnectionHolder.isConnectionReady()).thenReturn(true);
        when(sshConnectionHolder.getSshClient()).thenReturn(sshClient);

        FileTransferBatch ftb = FileTransferBatch.builder().deleteOriginal(true).status(TransferStatus.WAITING)
                .useCompression(false).fileTransfers(new ArrayList<>()).nodeId("123").build();
        FileTransfer file = FileTransfer.builder().filePath("C://candidates.txt").fileTransferBatch(ftb).build();
        ftb.getFileTransfers().add(file);
        ftb = ftbRepository.save(ftb);

        pinger.run();
        Message message = argumentCaptor.getValue();

        assertNotNull(message);
        Ping ping = CGSON.fromJson(message.getPayloadJSON(), Ping.class);
        assertEquals(ping.getWaitingBatches().getBatches().size(), 1);
        Batch batch = ping.getWaitingBatches().getBatches().get(0);
        assertEquals(ftb.getId(), batch.getId());
        assertEquals(ftb.getNodeId(), batch.getNodeId());
    }
}

