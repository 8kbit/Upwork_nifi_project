package com.thoughtapps.droppoint.droppoint.unit;

import com.thoughtapps.droppoint.core.dto.Message;
import com.thoughtapps.droppoint.core.dto.MessageType;
import com.thoughtapps.droppoint.core.helpers.CGSON;
import com.thoughtapps.droppoint.core.messageExchange.config.SshClientConfig;
import com.thoughtapps.droppoint.core.messageExchange.config.SshServerConfig;
import com.thoughtapps.droppoint.core.messageExchange.highLevel.MessageProcessor;
import com.thoughtapps.droppoint.core.messageExchange.highLevel.SshServer;
import com.thoughtapps.droppoint.core.messageExchange.highLevel.SshServerImpl;
import com.thoughtapps.droppoint.droppoint.helpers.SshConfigHelper;
import com.thoughtapps.droppoint.droppoint.schedule.SshConnectionHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Created by zaskanov on 17.04.2017.
 */
@Test
public class SshConnectionHolderTest extends AbstractTransactionalTest {

    @Autowired
    ApplicationContext context;

    @Autowired
    SshConfigHelper sshConfigHelper;

    @Test(timeOut = 10000, enabled = false)
    public void testServerNotAvailable() {
        SshConnectionHolder holder = context.getBean(SshConnectionHolder.class);
        holder.setIsConnectionAllowed(true);

        holder.run();

        assertFalse(holder.isConnectionReady());
    }

    @Test(timeOut = 10000)
    public void testConnectionOpened() {
        MessageProcessor portProcessor = new MessageProcessor() {
            @Override
            public Message processMessage(Message request) {
                return Message.builder().type(MessageType.OK).payloadJSON(CGSON.toJson(100)).build();
            }
        };
        SshClientConfig clientConfig = sshConfigHelper.createSshClientConfig();
        SshServerConfig serverConfig = SshServerConfig.builder().host(clientConfig.getHost()).port(clientConfig.getPort())
                .username(clientConfig.getUsername()).password(clientConfig.getPassword()).sftpEnabled(false).build();
        SshServer server = new SshServerImpl(serverConfig,
                Collections.singletonMap(MessageType.PORT_REQUEST, portProcessor));
        server.init();

        SshConnectionHolder holder = context.getBean(SshConnectionHolder.class);
        holder.setIsConnectionAllowed(true);

        holder.run();

        assertTrue(holder.isConnectionReady());

        server.close();
    }

    @Test(timeOut = 10000, enabled = false)
    public void testConnectionReopened() {
        SshConnectionHolder holder = context.getBean(SshConnectionHolder.class);

        holder.run();
        assertFalse(holder.isConnectionReady());

        MessageProcessor portProcessor = new MessageProcessor() {
            @Override
            public Message processMessage(Message request) {
                return Message.builder().type(MessageType.OK).payloadJSON(CGSON.toJson(100)).build();
            }
        };
        SshClientConfig clientConfig = sshConfigHelper.createSshClientConfig();
        SshServerConfig serverConfig = SshServerConfig.builder().host(clientConfig.getHost()).port(clientConfig.getPort())
                .username(clientConfig.getUsername()).password(clientConfig.getPassword()).sftpEnabled(false).build();
        SshServer server = new SshServerImpl(serverConfig,
                Collections.singletonMap(MessageType.PORT_REQUEST, portProcessor));
        server.init();

        holder.run();
        assertTrue(holder.isConnectionReady());

        server.close();
    }
}
