package com.thoughtapps.droppoint.droppointnode;

import com.thoughtapps.droppoint.core.dto.*;
import com.thoughtapps.droppoint.core.helpers.CGSON;
import com.thoughtapps.droppoint.core.messageExchange.config.SshClientConfig;
import com.thoughtapps.droppoint.core.messageExchange.config.SshServerConfig;
import com.thoughtapps.droppoint.core.messageExchange.highLevel.*;
import com.thoughtapps.droppoint.droppointnode.helpers.SshConfigHelper;
import com.thoughtapps.droppoint.droppointnode.holders.DropPointInfo;
import com.thoughtapps.droppoint.droppointnode.holders.DropPointInfoHolder;
import com.thoughtapps.droppoint.droppointnode.holders.FetchQueueHolder;
import com.thoughtapps.droppoint.droppointnode.messageProcessors.PingMessageProcessor;
import com.thoughtapps.droppoint.droppointnode.messageProcessors.PortRequestMessageProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zaskanov on 08.04.2017.
 */

/**
 * Used to communicate to drop point (send instructions, receive files)
 */
@Slf4j
@Lazy
@Component
public class ControllerSshServer {

    private SshServerImpl embeddedServer;

    @Autowired
    public DropPointInfoHolder dropPointInfoHolder;

    @Autowired
    private PingMessageProcessor pingMessageProcessor;

    @Autowired
    private PortRequestMessageProcessor portRequestMessageProcessor;

    @Autowired
    private FetchQueueHolder queueHolder = new FetchQueueHolder();

    @Autowired
    private Configuration configuration;

    @Autowired
    private SshConfigHelper sshConfigHelper;

    //Send instruction to drop point specified in instruction
    public void sendInstructions(InstructionsContainer container) throws RuntimeException {
        log.info("Start sending new instructions");

        Message request = new Message(MessageType.INSTRUCTIONS, CGSON.toJson(container));

        for (String dropPointId : configuration.getDropPointIds()) {
            DropPointInfo dropPointInfo = dropPointInfoHolder.getInfo(dropPointId);
            if (dropPointInfo == null) {
                log.info("Can not send instruction to drop point {}, because it is not connected", dropPointId);
                continue;
            }

            SshClientConfig sshClientConfig = sshConfigHelper.createSshClientConfig(dropPointInfo.getDropPointId());
            log.info(sshClientConfig.toString());
            log.info(dropPointInfo.getDropPointId());
            try (SshClient sshClient = new SshClientImpl(sshClientConfig)) {
                sshClient.init();
                Message response = sshClient.sendMessage(request);

                log.info("Response to new instructions: {} from drop point {}", response.getType(), dropPointInfo.getDropPointId());
            } catch (RuntimeException e) {
                log.error("Error while sending new instructions to drop point: " + dropPointInfo.getDropPointId(), e);
            }
        }

        log.info("Finish sending new instructions");
    }

    //Notify drop point about successful batch processing
    public void markBatchReceived(Batch batch) throws RuntimeException {
        log.info("Start markBatchReceived");

        SshClientConfig sshClientConfig = sshConfigHelper.createSshClientConfig(batch.getDropPointId());
        try (SshClient sshClient = new SshClientImpl(sshClientConfig)) {
            sshClient.init();
            BatchesContainer container = BatchesContainer.builder().batches(new ArrayList<>(1)).build();
            container.getBatches().add(batch);
            Message request = new Message(MessageType.BATCH_RECEIVED, CGSON.toJson(container));
            Message response = sshClient.sendMessage(request);

            log.info("Finish markBatchReceived");
        } catch (RuntimeException e) {
            log.error("Error while sending markBatchReceived to drop point: " + batch.getDropPointId(), e);
            throw e;
        }
    }

    // Read file from drop point
    public void readFile(String path, String dropPointId, boolean deleteOriginal, boolean useCompression, SshClient.InputStreamCallback callback)
            throws RuntimeException {
        log.info("Start readFile");

        SshClientConfig sshClientConfig = sshConfigHelper.createSshClientConfig(dropPointId);
        if (useCompression) sshClientConfig.setUseCompression(true);
        try (SshClient sshClient = new SshClientImpl(sshClientConfig)) {
            sshClient.init();

            sshClient.readFile(path, callback);
            if (deleteOriginal) sshClient.removeFile(path);

            log.info("Finish readFile");
        } catch (RuntimeException e) {
            log.error("Error while readFile from drop point: " + dropPointId, e);
            throw e;
        }
    }

    //Get nest batch ready for processing
    public Batch getNextBatch(String nodeId) throws RuntimeException {
        log.info("Start getNextBatch");

        Batch batch = queueHolder.poll(nodeId);
        if (batch == null) return null;

        SshClientConfig sshClientConfig = sshConfigHelper.createSshClientConfig(batch.getDropPointId());
        try (SshClient sshClient = new SshClientImpl(sshClientConfig)) {
            sshClient.init();

            BatchesContainer container = BatchesContainer.builder().batches(new ArrayList<>(1)).build();
            container.getBatches().add(batch);

            //Ask for detailed information about batch
            Message request = new Message(MessageType.BATCH_INFO, CGSON.toJson(container));
            Message response = sshClient.sendMessage(request);
            batch = CGSON.fromJson(response.getPayloadJSON(), BatchesContainer.class).getBatches().get(0);

            log.info("Finish getNextBatch");
            return batch;
        } catch (RuntimeException e) {
            log.error("Error while sending getNextBatch to drop point: " + batch.getDropPointId(), e);
            throw e;
        }
    }

    @PostConstruct
    public void init() {
        SshServerConfig sshServerConfig = sshConfigHelper.createSshServerConfig();
        Map<MessageType, MessageProcessor> processors = new HashMap<>();
        processors.put(MessageType.PING, pingMessageProcessor);
        processors.put(MessageType.PORT_REQUEST, portRequestMessageProcessor);
        embeddedServer = new SshServerImpl(sshServerConfig, processors);
        embeddedServer.setListener(new SshServerListener() {
            @Override
            public void sessionClosed(Session session) {
                if (session.getProperties().get("dropPointId") != null) {
                    dropPointInfoHolder.removeInfo(session.getProperties().get("dropPointId").toString());
                    log.info("Close connection to {}", session.getProperties().get("dropPointId"));
                }
            }

            @Override
            public void tunnelEstablished(Session session, SshdSocketAddress local, SshdSocketAddress remote) {
                String dropPointId = dropPointInfoHolder.findDropPointByPort(local.getPort());
                if (dropPointId != null) {
                    session.getProperties().put("dropPointId", dropPointId);
                    DropPointInfo dropPointInfo = DropPointInfo.builder().dropPointId(dropPointId)
                            .lastPing(System.currentTimeMillis()).localPort(local.getPort()).build();
                    dropPointInfoHolder.putInfo(dropPointInfo);
                    log.info("Established connection to {}. local {}:{}",
                            dropPointId, local.getHostName(), local.getPort());
                } else try {
                    log.error("Error while establishing port forwarding. Assigned port not founded. Drop point id: {}", dropPointId);
                    session.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
        embeddedServer.init();
    }

    @PreDestroy
    public void destroy() {
        if (embeddedServer != null) embeddedServer.close();
        dropPointInfoHolder.clearInfo();
        queueHolder.clear();
    }
}
