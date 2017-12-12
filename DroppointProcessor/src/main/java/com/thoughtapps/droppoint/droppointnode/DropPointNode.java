package com.thoughtapps.droppoint.droppointnode;

import com.thoughtapps.droppoint.core.dto.InstructionsContainer;
import com.thoughtapps.droppoint.core.dto.Message;
import com.thoughtapps.droppoint.core.dto.MessageType;
import com.thoughtapps.droppoint.core.helpers.CGSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;

import java.io.IOException;

/**
 * Created by zaskanov on 18.04.2017.
 */

/**
 * Used only for testing purposes
 */
@Slf4j
public class DropPointNode implements Runnable {
    public static void main(String[] args) {
        DropPointNode dropPointNode = new DropPointNode();
        dropPointNode.run();
    }

    @Override
    public void run() {
        AnnotationConfigApplicationContext springContext = new AnnotationConfigApplicationContext(AppConfig.class);
        Environment env = springContext.getEnvironment();

        Configuration configuration = springContext.getBean(Configuration.class);
        configuration.setNodeId(env.getProperty(Configuration.NODE_ID));
        configuration.setHost(env.getProperty(Configuration.HOST));
        configuration.setPort(env.getProperty(Configuration.PORT, Integer.class));
        configuration.setUsername(env.getProperty(Configuration.USERNAME));
        configuration.setPassword(env.getProperty(Configuration.PASSWORD));

        configuration.setMinDroppointPort(env.getProperty(Configuration.MIN_DROP_POINT_PORT, Integer.class));
        configuration.setMaxDroppointPort(env.getProperty(Configuration.MAX_DROP_POINT_PORT, Integer.class));

        configuration.setDropPointUsername(env.getProperty(Configuration.DROP_POINT_USERNAME));
        configuration.setDropPointPassword(env.getProperty(Configuration.DROP_POINT_PASSWORD));

        /**
         trigger ControllerSshServer initialization. ControllerSshServer use lazy initialization because app need to fill
         * configuration bean first
         */
        ControllerSshServer controllerSshServer = springContext.getBean(ControllerSshServer.class);

        try {
            Thread.currentThread().sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String payload = null;
        try {
            payload = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("ExampleInstructionsRequest.json"));
            InstructionsContainer instructions = CGSON.fromJson(payload, InstructionsContainer.class);
            instructions.setNodeId("123");
            controllerSshServer.sendInstructions(instructions);
        } catch (IOException e) {
            e.printStackTrace();
        }
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }
}
