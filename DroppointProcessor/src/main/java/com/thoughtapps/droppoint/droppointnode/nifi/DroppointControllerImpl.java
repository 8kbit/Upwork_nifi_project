package com.thoughtapps.droppoint.droppointnode.nifi;

import com.thoughtapps.droppoint.core.dto.Batch;
import com.thoughtapps.droppoint.core.dto.InstructionsContainer;
import com.thoughtapps.droppoint.core.messageExchange.highLevel.SshClient;
import com.thoughtapps.droppoint.droppointnode.AppConfig;
import com.thoughtapps.droppoint.droppointnode.Configuration;
import com.thoughtapps.droppoint.droppointnode.ControllerSshServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnDisabled;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.components.Validator;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;

import java.util.*;

/**
 * Created by zaskanov on 02.04.2017.
 */
@Slf4j
@Tags({"custom", "droppoint"})
@CapabilityDescription("Provides a controller service to receive connections from droppoints")
public class DroppointControllerImpl extends AbstractControllerService implements DroppointController {

    private static final PropertyDescriptor HOSTNAME = new PropertyDescriptor.Builder()
            .name("Hostname")
            .description("The fully-qualified hostname or IP address of the host to fetch the data from")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .required(true)
            .defaultValue("0.0.0.0")
            .build();
    private static final PropertyDescriptor PORT = new PropertyDescriptor.Builder()
            .name("Port")
            .description("The port to connect to on the remote host to fetch the data from")
            .addValidator(StandardValidators.PORT_VALIDATOR)
            .expressionLanguageSupported(true)
            .required(true)
            .defaultValue("23")
            .build();
    private static final PropertyDescriptor USERNAME = new PropertyDescriptor.Builder()
            .name("Username")
            .description("Username")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .required(true)
            .defaultValue("admin")
            .build();
    private static final PropertyDescriptor PASSWORD = new PropertyDescriptor.Builder()
            .name("Password")
            .description("Password for the user account")
            .addValidator(Validator.VALID)
            .required(true)
            .sensitive(true)
            .defaultValue("admin")
            .build();
    private static final PropertyDescriptor MIN_DROP_POINT_PORT = new PropertyDescriptor.Builder()
            .name("MinDropPointPort")
            .description("Minimal port of assigned drop point ports")
            .addValidator(StandardValidators.PORT_VALIDATOR)
            .required(true)
            .defaultValue("100")
            .build();
    private static final PropertyDescriptor MAX_DROP_POINT_PORT = new PropertyDescriptor.Builder()
            .name("MaxDropPointPort")
            .description("Maximal port of assigned drop point ports")
            .addValidator(StandardValidators.PORT_VALIDATOR)
            .required(true)
            .defaultValue("120")
            .build();
    private static final PropertyDescriptor DROP_POINT_ID_LIST = new PropertyDescriptor.Builder()
            .name("DropPointIdList")
            .description("Comma separated list of drop points")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .required(true)
            .defaultValue("ewXJlcMO2O")
            .build();

    private static final List<PropertyDescriptor> serviceProperties;

    static {
        final List<PropertyDescriptor> properties = new ArrayList<>();
        properties.add(HOSTNAME);
        properties.add(PORT);
        properties.add(USERNAME);
        properties.add(PASSWORD);
        properties.add(MIN_DROP_POINT_PORT);
        properties.add(MAX_DROP_POINT_PORT);
        properties.add(DROP_POINT_ID_LIST);
        serviceProperties = Collections.unmodifiableList(properties);
    }

    private AnnotationConfigApplicationContext springContext;

    @Override
    protected Collection<ValidationResult> customValidate(ValidationContext validationContext) {
        List<ValidationResult> problems = new ArrayList<>(super.customValidate(validationContext));

        int minPort = validationContext.getProperty(MIN_DROP_POINT_PORT).asInteger();
        int maxPort = validationContext.getProperty(MAX_DROP_POINT_PORT).asInteger();

        if (maxPort < minPort)
            problems.add((new ValidationResult.Builder()).subject(MAX_DROP_POINT_PORT.getName())
                    .input(validationContext.getProperty(MAX_DROP_POINT_PORT).getValue()).valid(false)
                    .explanation("Max port must not be lower than Min port").build());

        return problems;
    }

    @OnEnabled
    public void onConfigured(final ConfigurationContext context) throws InitializationException {
        getLogger().info("Drop point node controller initializing...");

        springContext = new AnnotationConfigApplicationContext(AppConfig.class);
        Environment env = springContext.getEnvironment();

        Configuration configuration = springContext.getBean(Configuration.class);
        configuration.setHost(context.getProperty(HOSTNAME).getValue());
        configuration.setPort(context.getProperty(PORT).asInteger());
        configuration.setUsername(context.getProperty(USERNAME).getValue());
        configuration.setPassword(context.getProperty(PASSWORD).getValue());

        configuration.setMinDroppointPort(context.getProperty(MIN_DROP_POINT_PORT).asInteger());
        configuration.setMaxDroppointPort(context.getProperty(MAX_DROP_POINT_PORT).asInteger());
        configuration.setDropPointIds(new HashSet<>(
                Arrays.asList(StringUtils.deleteWhitespace(context.getProperty(DROP_POINT_ID_LIST).getValue()).split(","))));

        configuration.setDropPointUsername(env.getProperty(Configuration.DROP_POINT_USERNAME));
        configuration.setDropPointPassword(env.getProperty(Configuration.DROP_POINT_PASSWORD));

        //trigger ControllerSshServer initialization
        springContext.getBean(ControllerSshServer.class);

        getLogger().info("Drop point node controller. Initialization finished");
    }

    @OnDisabled
    public void onDestroy(ConfigurationContext context) {
        getLogger().info("Drop point node controller. Stopping...");

        if (springContext != null) {
            springContext.destroy();
            springContext = null;
        }

        getLogger().info("Drop point node controller. Stopped");
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return serviceProperties;
    }

    @Override
    public void sendInstructions(InstructionsContainer container) throws RuntimeException {
        ControllerSshServer controllerSshServer = springContext.getBean(ControllerSshServer.class);
        controllerSshServer.sendInstructions(container);
    }

    @Override
    public void markBatchReceived(Batch batch) throws RuntimeException {
        ControllerSshServer controllerSshServer = springContext.getBean(ControllerSshServer.class);
        controllerSshServer.markBatchReceived(batch);
    }

    @Override
    public void readFile(String path, String dropPointId, boolean deleteOriginal, boolean useCompression, SshClient.InputStreamCallback callback) throws RuntimeException {
        ControllerSshServer controllerSshServer = springContext.getBean(ControllerSshServer.class);
        controllerSshServer.readFile(path, dropPointId, deleteOriginal, useCompression, callback);
    }

    @Override
    public Batch getNextBatch(String nodeId) throws RuntimeException {
        ControllerSshServer controllerSshServer = springContext.getBean(ControllerSshServer.class);
        return controllerSshServer.getNextBatch(nodeId);
    }
}
