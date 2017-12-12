package com.thoughtapps.droppoint.droppoint.schedule;

import com.thoughtapps.droppoint.core.dto.Message;
import com.thoughtapps.droppoint.core.dto.MessageType;
import com.thoughtapps.droppoint.core.helpers.CGSON;
import com.thoughtapps.droppoint.core.messageExchange.config.SshClientConfig;
import com.thoughtapps.droppoint.core.messageExchange.highLevel.SshClient;
import com.thoughtapps.droppoint.droppoint.helpers.SshConfigHelper;
import com.thoughtapps.droppoint.droppoint.service.ConfigurationService;
import com.thoughtapps.droppoint.droppoint.service.SshClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * Created by zaskanov on 17.04.2017.
 */

/**
 * Keep open ssh connection to drop point processor. Reconnect if connection is closed
 */
@Slf4j
@Component
public class SshConnectionHolder extends AbstractConfigScheduledJob {

    private SshClient sshClient;

    @Autowired
    private SshConfigHelper sshConfigHelper;

    @Autowired
    private ConfigurationService confService;

    @Autowired
    private SshClientFactory sshClientFactory;

    //Open connection only when drop point is launched from UI
    private boolean isConnectionAllowed = false;

    public void setIsConnectionAllowed(boolean isConnectionAllowed) {
        this.isConnectionAllowed = isConnectionAllowed;
    }

    public boolean isConnectionAllowed() {
        return isConnectionAllowed;
    }

    public boolean isConnectionReady() {
        return sshClient != null && sshClient.isOpen();
    }

    public SshClient getSshClient() {
        return sshClient;
    }

    @Override
    protected int getIntervalSec() {
        return confService.getIntPropertyValue(ConfigurationService.DROP_POINT_RECONNECTION_INTERVAL_SEC);
    }

    @PreDestroy
    public void destroy() {
        if (sshClient != null) {
            sshClient.close();
            sshClient = null;
        }
    }

    // Check connection status and reconnect if needed
    @Override
    public void run() {
        if (!isConnectionAllowed) return;

        try {
            if (sshClient == null) {
                SshClientConfig clientConfig = sshConfigHelper.createSshClientConfig();
                sshClient = sshClientFactory.createSshClient(clientConfig);
            }

            if (!sshClient.isOpen()) {
                sshClient.close();
                sshClient.init();

                //Ask drop point processor about port assigned for this drop point
                Message response = sshClient.sendMessage(Message.builder()
                        .type(MessageType.PORT_REQUEST)
                        .payloadJSON(CGSON.toJson(confService.getPropertyValue(ConfigurationService.DROP_POINT_ID)))
                        .build());

                Integer forwardPort = CGSON.fromJson(response.getPayloadJSON(), Integer.class);
                if (response.getType() == MessageType.ERROR) {
                    sshClient.close();
                    return;
                }

                //Create port forwarding (forward port from drop point processor to drop point)
                sshClient.forwardRemotePort("127.0.0.1", forwardPort,
                        confService.getPropertyValue(ConfigurationService.DROP_POINT_HOSTNAME),
                        confService.getIntPropertyValue(ConfigurationService.DROP_POINT_PORT));
            }
        } catch (Exception e) {
            if (sshClient != null) {
                sshClient.close();
                sshClient = null;
            }
            if (log.isDebugEnabled())
                log.debug("Failed to connect to remote ssh server", e);
            else
                log.info("Processor not reachable");
        }
    }
}
