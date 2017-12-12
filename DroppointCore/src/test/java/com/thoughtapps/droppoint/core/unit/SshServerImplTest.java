package com.thoughtapps.droppoint.core.unit;

import com.thoughtapps.droppoint.core.dto.Message;
import com.thoughtapps.droppoint.core.dto.MessageType;
import com.thoughtapps.droppoint.core.messageExchange.config.SshClientConfig;
import com.thoughtapps.droppoint.core.messageExchange.config.SshServerConfig;
import com.thoughtapps.droppoint.core.messageExchange.highLevel.*;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;


/**
 * Created by zaskanov on 01.04.2017.
 */
@Test
public class SshServerImplTest {

    @Test(timeOut = 10000)
    public void testSshServerListener() throws IOException {
        SshServerConfig serverConfig = SshServerConfig.builder().host("127.0.0.2").port(100)
                .username("admin").password("admin").sftpEnabled(true).sftpRootDir("").build();
        SshServer server = new SshServerImpl(serverConfig, Collections.emptyMap());

        SshServerListenerExt listener = spy(SshServerListenerExt.class);
        listener.setClientId("test");
        server.setListener(listener);
        server.init();

        SshClientConfig clientConfig = SshClientConfig.builder().host(serverConfig.getHost()).port(serverConfig.getPort())
                .username(serverConfig.getUsername()).password(serverConfig.getPassword()).build();
        SshClient sshClient = new SshClientImpl(clientConfig);
        sshClient.init();
        sshClient.forwardRemotePort(serverConfig.getHost(), 0,
                serverConfig.getHost(), 0);

        sshClient.close();
        server.close();

        ArgumentCaptor<Session> tunSessionCaptor = ArgumentCaptor.forClass(Session.class);
        ArgumentCaptor<SshdSocketAddress> tunSocketCaptor = ArgumentCaptor.forClass(SshdSocketAddress.class);
        ArgumentCaptor<Session> closeCaptor = ArgumentCaptor.forClass(Session.class);
        verify(listener).tunnelEstablished(tunSessionCaptor.capture(), tunSocketCaptor.capture(), any());
        verify(listener).sessionClosed(closeCaptor.capture());

        verify(listener).tunnelEstablished(any(), any(), any());
        verify(listener).sessionClosed(any());

        assertEquals("test", tunSessionCaptor.getValue().getProperties().get("dropPointId"));
    }

    @Test(timeOut = 10000)
    public void testMessageProcessing() throws IOException {
        SshServerConfig serverConfig = SshServerConfig.builder().host("127.0.0.3").port(100)
                .username("admin").password("admin").sftpEnabled(true).sftpRootDir("").build();
        MessageProcessor pingProcessor = new MessageProcessor() {
            @Override
            public Message processMessage(Message request) {
                return Message.OK;
            }
        };

        SshClientConfig clientConfig = SshClientConfig.builder().host(serverConfig.getHost()).port(serverConfig.getPort())
                .useCompression(true).username(serverConfig.getUsername()).password(serverConfig.getPassword()).build();
        SshClient sshClient = new SshClientImpl(clientConfig);
        SshServer sshServer = new SshServerImpl(serverConfig, Collections.singletonMap(MessageType.PING, pingProcessor));

        sshServer.init();
        sshClient.init();

        Message response = sshClient.sendMessage(Message.builder().type(MessageType.PING).payloadJSON(null).build());

        sshClient.close();
        sshServer.close();

        assertEquals(Message.OK, response);
    }

    @Test(timeOut = 20000)
    public void testPortForwarding() throws IOException {
        SshServerConfig nodeServerConfig = SshServerConfig.builder().host("127.0.0.5").port(100)
                .username("admin").password("admin").sftpEnabled(true).sftpRootDir("").build();
        SshServerConfig droppointServerConfig = SshServerConfig.builder().host("127.0.0.6").port(100)
                .username("admin").password("admin").sftpEnabled(true).sftpRootDir("").build();

        SshClientConfig nodeClientConfig = SshClientConfig.builder()
                .host(nodeServerConfig.getHost()).port(101)
                .username(droppointServerConfig.getUsername()).password(droppointServerConfig.getPassword())
                .build();
        SshClientConfig droppointClientConfig = SshClientConfig.builder()
                .host(nodeServerConfig.getHost()).port(nodeServerConfig.getPort())
                .username(nodeServerConfig.getUsername()).password(nodeServerConfig.getPassword())
                .build();

        MessageProcessor droppointProcessor = new MessageProcessor() {
            @Override
            public Message processMessage(Message request) {
                return Message.OK;
            }
        };

        SshServer nodeServer = new SshServerImpl(nodeServerConfig, Collections.emptyMap());
        SshServer droppointServer = new SshServerImpl(droppointServerConfig,
                Collections.singletonMap(MessageType.PING, droppointProcessor));
        nodeServer.init();
        droppointServer.init();

        SshClient droppointClient = new SshClientImpl(droppointClientConfig);
        droppointClient.init();
        droppointClient.forwardRemotePort(nodeClientConfig.getHost(), nodeClientConfig.getPort(),
                droppointServerConfig.getHost(), droppointServerConfig.getPort());

        SshClient nodeClient = new SshClientImpl(nodeClientConfig);
        nodeClient.init();

        Message response = nodeClient.sendMessage(Message.builder().type(MessageType.PING).payloadJSON(null).build());

        nodeClient.close();
        droppointClient.close();
        nodeServer.close();
        droppointServer.close();

        assertEquals(Message.OK, response);
    }

    @Test(timeOut = 20000)
    public void testReadFile() throws IOException {
        String rootDir = getSFTPRoot().getAbsolutePath();
        SshServerConfig serverConfig = SshServerConfig.builder().host("127.0.0.6").port(100)
                .username("admin").password("admin").sftpEnabled(true).sftpRootDir(rootDir).build();
        SshClientConfig clientConfig = SshClientConfig.builder()
                .host(serverConfig.getHost()).port(serverConfig.getPort())
                .username(serverConfig.getUsername()).password(serverConfig.getPassword())
                .build();
        SshServer server = new SshServerImpl(serverConfig, Collections.emptyMap());
        SshClient client = new SshClientImpl(clientConfig);
        server.init();
        client.init();

        final AtomicReference<String> fileContentRef = new AtomicReference<>();
        String fileContent = IOUtils.toString(this.getClass().getClassLoader()
                .getResourceAsStream("ftpRoot/1/FinanceReport.csv"));
        client.readFile("1/FinanceReport.csv", new SshClient.InputStreamCallback() {
            @Override
            public void read(InputStream in) {
                try {
                    fileContentRef.set(IOUtils.toString(in));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        client.close();
        server.close();

        assertEquals(fileContentRef.get(), fileContent);
    }

    @Test(timeOut = 20000)
    public void testRemoveFile() throws IOException {
        String rootDir = getSFTPRoot().getAbsolutePath();
        String content = "test";
        File file = new File(Paths.get(rootDir, "test.txt").toString());
        FileUtils.writeStringToFile(file, content, Charsets.UTF_8);

        SshServerConfig serverConfig = SshServerConfig.builder().host("127.0.0.7").port(100)
                .username("admin").password("admin").sftpEnabled(true).sftpRootDir(rootDir).build();
        SshClientConfig clientConfig = SshClientConfig.builder()
                .host(serverConfig.getHost()).port(serverConfig.getPort())
                .username(serverConfig.getUsername()).password(serverConfig.getPassword())
                .build();
        SshServer server = new SshServerImpl(serverConfig, Collections.emptyMap());
        SshClient client = new SshClientImpl(clientConfig);
        server.init();
        client.init();

        client.removeFile(file.getName());
        client.close();
        server.close();

        file = new File(file.getAbsolutePath());
        assertTrue(!file.exists());
    }

    public static class SshServerListenerExt implements SshServerListener {

        private String clientId = "";

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        @Override
        public void sessionClosed(Session session) {
        }

        @Override
        public void tunnelEstablished(Session session, SshdSocketAddress local, SshdSocketAddress remote) {
            session.getProperties().put("dropPointId", clientId);
        }
    }

    private File getSFTPRoot() {
        try {
            return new File(this.getClass().getClassLoader().getResource("ftpRoot").toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
