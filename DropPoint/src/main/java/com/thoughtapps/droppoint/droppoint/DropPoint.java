package com.thoughtapps.droppoint.droppoint;

import com.thoughtapps.droppoint.core.dto.MessageType;
import com.thoughtapps.droppoint.core.messageExchange.config.SshServerConfig;
import com.thoughtapps.droppoint.core.messageExchange.highLevel.MessageProcessor;
import com.thoughtapps.droppoint.core.messageExchange.highLevel.SshServerImpl;
import com.thoughtapps.droppoint.droppoint.helpers.SshConfigHelper;
import com.thoughtapps.droppoint.droppoint.ui.AppUI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zaskanov on 30.03.2017.
 */

/**
 * Entry point for drop point
 */
@Slf4j
@Service
public class DropPoint implements Runnable {
    @Autowired
    private MessageProcessor instructionsProcessor;

    @Autowired
    private MessageProcessor markBatchReceivedProcessor;

    @Autowired
    private MessageProcessor batchInfoProcessor;

    @Autowired
    private SshConfigHelper sshConfHelper;

    private static AnnotationConfigApplicationContext springContext;

    public static ApplicationContext getSpringContext() {
        return springContext;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        springContext = new AnnotationConfigApplicationContext(AppConfig.class);
        DropPoint dropPoint = springContext.getBean(DropPoint.class);
        dropPoint.run();
        AppUI.main(args);
        System.exit(0);
    }

    @Override
    public void run() {
        SshServerConfig serverConfig = sshConfHelper.createSshServerConfig();
        Map<MessageType, MessageProcessor> processors = new HashMap<>();
        processors.put(MessageType.INSTRUCTIONS, instructionsProcessor);
        processors.put(MessageType.BATCH_RECEIVED, markBatchReceivedProcessor);
        processors.put(MessageType.BATCH_INFO, batchInfoProcessor);

        SshServerImpl sshServer = new SshServerImpl(serverConfig, processors);
        sshServer.init();
    }
}
