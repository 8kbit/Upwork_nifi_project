package com.thoughtapps.droppoint.droppoint.schedule;

import com.thoughtapps.droppoint.core.dto.BatchesContainer;
import com.thoughtapps.droppoint.core.dto.Message;
import com.thoughtapps.droppoint.core.dto.MessageType;
import com.thoughtapps.droppoint.core.dto.Ping;
import com.thoughtapps.droppoint.core.helpers.CGSON;
import com.thoughtapps.droppoint.droppoint.converters.FileTransferBatchToBatchConverter;
import com.thoughtapps.droppoint.droppoint.helpers.SshConfigHelper;
import com.thoughtapps.droppoint.droppoint.model.FileTransferBatch;
import com.thoughtapps.droppoint.droppoint.model.TransferStatus;
import com.thoughtapps.droppoint.droppoint.repositories.FileTransferBatchRepository;
import com.thoughtapps.droppoint.droppoint.service.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zaskanov on 02.04.2017.
 */

/**
 * Periodically sends ping to server along with information about available batches
 */
@Slf4j
@Component
public class PingerJob extends AbstractConfigScheduledJob {
    @Autowired
    SshConfigHelper sshConfHelper;

    @Autowired
    ConfigurationService confService;

    @Autowired
    FileTransferBatchRepository ftbRepository;

    @Autowired
    FileTransferBatchToBatchConverter converter;

    @Autowired
    SshConnectionHolder sshConnectionHolder;

    @Override
    public void run() {
        if (!sshConnectionHolder.isConnectionAllowed()) return;

        try {
            log.info("Start pinger job");

            //Only if connection to drop point processor is ready
            if (!sshConnectionHolder.isConnectionReady()) {
                log.info("Ssh connection is not ready");
                return;
            }

            List<FileTransferBatch> batchList = ftbRepository.findByStatusOrderByCreatedDateAsc(TransferStatus.WAITING);
            BatchesContainer container = BatchesContainer.builder().batches(new ArrayList<>()).build();
            for (FileTransferBatch fileTransferBatch : batchList) {
                container.getBatches().add(converter.convert(fileTransferBatch, true));
            }

            //Send ping to drop point processor along with information about batches ready to process
            Ping ping = Ping.builder().dropPointId(confService.getPropertyValue(ConfigurationService.DROP_POINT_ID))
                    .waitingBatches(container).build();
            Message request = new Message(MessageType.PING, CGSON.toJson(ping));

            sshConnectionHolder.getSshClient().sendMessage(request);

        } catch (Throwable e) {
            log.error("Ping error: {}", e.getMessage());
        } finally {
            log.info("Finish pinger job");
        }
    }

    @Override
    protected int getIntervalSec() {
        return confService.getIntPropertyValue(ConfigurationService.DROP_POINT_PING_INTERVAL);
    }
}
