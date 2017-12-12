package com.thoughtapps.droppoint.droppointnode;

import com.thoughtapps.droppoint.core.dto.Batch;
import com.thoughtapps.droppoint.core.dto.BatchesContainer;
import com.thoughtapps.droppoint.core.dto.Message;
import com.thoughtapps.droppoint.core.dto.MessageType;
import com.thoughtapps.droppoint.core.helpers.CGSON;
import com.thoughtapps.droppoint.core.messageExchange.config.SshServerConfig;
import com.thoughtapps.droppoint.core.messageExchange.highLevel.*;
import com.thoughtapps.droppoint.droppointnode.holders.DropPointInfoHolder;
import com.thoughtapps.droppoint.droppointnode.holders.FetchQueueHolder;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Created by zaskanov on 19.04.2017.
 */
@Test
@TestPropertySource({"/application.properties", "/test.properties"})
@ContextConfiguration(classes = {AppConfig.class})
public class ControllerSshServerTest extends AbstractTestNGSpringContextTests {

    @Autowired
    Configuration configuration;

    @Autowired
    Environment env;

    @Autowired
    ApplicationContext context;

    @Autowired
    private FetchQueueHolder queueHolder;

    @Autowired
    private DropPointInfoHolder dropPointInfoHolder;

    @Test
    public void sendInstructionsTest() {
        SshServerConfig dropServerConfig = SshServerConfig.builder()
                .host("127.0.0.1")
                .port(dropPointInfoHolder.getDropPointPort(env.getProperty("sftp.droppoint.id")))
                .username(configuration.getDropPointUsername())
                .password(configuration.getDropPointPassword())
                .sftpRootDir(getSFTPRoot().getAbsolutePath())
                .sftpEnabled(true).build();
        Batch batch = Batch.builder().id(1L).dropPointId(env.getProperty("sftp.droppoint.id")).nodeId("123").build();
        MessageProcessor batchInfoProcessor = new AbstractMessageProcessor() {
            @Override
            protected Message processInternal(Message request) throws Exception {
                BatchesContainer container = CGSON.fromJson(request.getPayloadJSON(), BatchesContainer.class);
                container.getBatches().add(batch);
                return Message.builder().type(MessageType.OK).payloadJSON(CGSON.toJson(container)).build();
            }
        };
        SshServer dropPointServer = new SshServerImpl(dropServerConfig,
                Collections.singletonMap(MessageType.BATCH_INFO, batchInfoProcessor));
        dropPointServer.init();

        ControllerSshServer nodeSshServer = context.getBean(ControllerSshServer.class);

        queueHolder.addAll(Collections.singletonList(batch));
        Batch receivedBatch = nodeSshServer.getNextBatch("123");

        dropPointServer.close();
        nodeSshServer.destroy();

        assertEquals(batch, receivedBatch);
    }

    @Test
    public void testReadFile() throws IOException {
        SshServerConfig dropServerConfig = SshServerConfig.builder()
                .host("127.0.0.1")
                .port(dropPointInfoHolder.getDropPointPort(env.getProperty("sftp.droppoint.id")))
                .username(configuration.getDropPointUsername())
                .password(configuration.getDropPointPassword())
                .sftpRootDir(getSFTPRoot().getAbsolutePath())
                .sftpEnabled(true).build();
        SshServer dropPointServer = new SshServerImpl(dropServerConfig, Collections.emptyMap());
        dropPointServer.init();

        ControllerSshServer nodeSshServer = context.getBean(ControllerSshServer.class);

        boolean exceptionThrown = false;
        try {
            nodeSshServer.readFile("2.txt", env.getProperty("sftp.droppoint.id"), false, false, new SshClient.InputStreamCallback() {
                @Override
                public void read(InputStream in) {
                }
            });
        } catch (RuntimeException e) {
            exceptionThrown = true;
        }

        AtomicReference<String> fileContent = new AtomicReference<>();
        String actualFileContent = IOUtils.toString(this.getClass().getClassLoader()
                .getResourceAsStream("ftpRoot/1level/3.txt"));
        nodeSshServer.readFile("1level/3.txt", env.getProperty("sftp.droppoint.id"), false, false, new SshClient.InputStreamCallback() {
            @Override
            public void read(InputStream in) {
                try {
                    fileContent.set(IOUtils.toString(in));
                } catch (IOException e) {
                }
            }
        });

        dropPointServer.close();
        nodeSshServer.destroy();

        assertTrue(exceptionThrown);
        assertEquals(actualFileContent, fileContent.get());
    }


    @BeforeClass
    public void init() {
        configuration.setNodeId(env.getProperty(Configuration.NODE_ID));
        configuration.setHost(env.getProperty(Configuration.HOST));
        configuration.setPort(env.getProperty(Configuration.PORT, Integer.class));
        configuration.setUsername(env.getProperty(Configuration.USERNAME));
        configuration.setPassword(env.getProperty(Configuration.PASSWORD));

        configuration.setMinDroppointPort(env.getProperty(Configuration.MIN_DROP_POINT_PORT, Integer.class));
        configuration.setMaxDroppointPort(env.getProperty(Configuration.MAX_DROP_POINT_PORT, Integer.class));

        configuration.setDropPointUsername(env.getProperty(Configuration.DROP_POINT_USERNAME));
        configuration.setDropPointPassword(env.getProperty(Configuration.DROP_POINT_PASSWORD));
    }

    private File getSFTPRoot() {
        try {
            return new File(this.getClass().getClassLoader().getResource("ftpRoot").toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
