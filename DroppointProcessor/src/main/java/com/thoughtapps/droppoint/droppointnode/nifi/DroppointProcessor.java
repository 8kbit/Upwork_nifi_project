package com.thoughtapps.droppoint.droppointnode.nifi;

import com.google.gson.JsonSyntaxException;
import com.thoughtapps.droppoint.core.dto.Batch;
import com.thoughtapps.droppoint.core.dto.File;
import com.thoughtapps.droppoint.core.dto.InstructionsContainer;
import com.thoughtapps.droppoint.core.helpers.CGSON;
import com.thoughtapps.droppoint.core.messageExchange.highLevel.SshClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.InputRequirement.Requirement;
import org.apache.nifi.annotation.behavior.TriggerWhenEmpty;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.lifecycle.OnStopped;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by zaskanov on 02.04.2017.
 */
@Slf4j
@TriggerWhenEmpty
@InputRequirement(Requirement.INPUT_FORBIDDEN)
@Tags({"sftp", "custom", "droppoint"})
@CapabilityDescription("Fetches the content of a file from a remote SFTP server and overwrites the contents of an incoming FlowFile with the content of the remote file.")
@WritesAttributes({
        @WritesAttribute(attribute = "sftp.client.id", description = "Unique id of remote sftp server"),
        @WritesAttribute(attribute = "filename", description = "The name of the remote file that was pulled"),
        @WritesAttribute(attribute = "path", description = "If the Remote File contains a directory name, that directory name will be added to the FlowFile using the 'path' attribute")
})
public class DroppointProcessor extends AbstractProcessor {

    private DroppointController controller;
    private long lastInstructionLookup = 0;

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {

        final List<PropertyDescriptor> properties = new ArrayList<>();
        properties.add(NODE_SERVICE);

        properties.add(INSTRUCTION_POLLING_INTERVAL);
        properties.add(INSTRUCTION_JSON);
        return properties;
    }

    private static final PropertyDescriptor NODE_SERVICE = new PropertyDescriptor.Builder()
            .name("Drop point node controller")
            .description("The Controller Service that is used to receive connections from droppoints")
            .required(true)
            .identifiesControllerService(DroppointController.class)
            .build();

    private static final PropertyDescriptor INSTRUCTION_POLLING_INTERVAL = new PropertyDescriptor.Builder()
            .name("Instructions Polling Interval")
            .description("Indicates how long to wait before performing next instruction broadcast")
            .required(true)
            .addValidator(StandardValidators.TIME_PERIOD_VALIDATOR)
            .defaultValue("4 sec")
            .build();
    private static final PropertyDescriptor INSTRUCTION_JSON = new PropertyDescriptor.Builder()
            .name("Instruction JSON")
            .description("JSON containing instructions for drop point")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .required(true)
            .expressionLanguageSupported(false)
            .build();

    private static final Relationship REL_FILE_SUCCESS = new Relationship.Builder()
            .name("file.success")
            .description("All FlowFiles that are received are routed to success")
            .build();
    private static final Relationship REL_FILE_FAILURE = new Relationship.Builder()
            .name("file.failure")
            .description("Any FlowFile that could not be fetched from the remote server due to a communications failure will be transferred to this Relationship.")
            .build();

    @Override
    public Set<Relationship> getRelationships() {
        final Set<Relationship> relationships = new HashSet<>();
        relationships.add(REL_FILE_SUCCESS);
        relationships.add(REL_FILE_FAILURE);
        return relationships;
    }

    @Override
    protected Collection<ValidationResult> customValidate(ValidationContext validationContext) {
        List<ValidationResult> problems = new ArrayList<>(super.customValidate(validationContext));

        String instructionJSON = validationContext.getProperty(INSTRUCTION_JSON).getValue();
        try {
            CGSON.fromJson(instructionJSON, InstructionsContainer.class);
        } catch (JsonSyntaxException e) {
            problems.add((new ValidationResult.Builder()).subject(INSTRUCTION_JSON.getName())
                    .valid(false)
                    .explanation("Instruction JSON is not valid").build());
            log.error("Instruction JSON is not valid");
        }

        return problems;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {
        getLogger().info("Drop point node initializing...");

        controller = context.getProperty(NODE_SERVICE).asControllerService(DroppointController.class);

        getLogger().info("Drop point node. Initialization finished");
    }

    @OnStopped
    public void cleanup() {
        getLogger().info("Drop point node. Stopping...");

        controller = null;

        getLogger().info("Drop point node. Stopped");
    }

    public void processBatch(final ProcessSession session, final Batch batch) {
        log.debug("Start batch processing....");

        for (File file : batch.getFiles()) {
            FlowFile flowFile = session.create();
            flowFile = session.putAttribute(flowFile, "sftp.client.id", batch.getDropPointId());
            flowFile = session.putAttribute(flowFile, "filename", FilenameUtils.getName(file.getFilePath()));
            flowFile = session.putAttribute(flowFile, "path", FilenameUtils.getFullPath(file.getFilePath()));
            AtomicReference<FlowFile> flowFileRef = new AtomicReference<>(flowFile);

            try {
                controller.readFile(file.getFilePath(), batch.getDropPointId(), batch.getDeleteOriginal(), batch.getUseCompression(),
                        new SshClient.InputStreamCallback() {
                            @Override
                            public void read(InputStream in) {
                                flowFileRef.set(session.importFrom(in, flowFileRef.get()));
                            }
                        });
                session.transfer(flowFileRef.get(), REL_FILE_SUCCESS);
            } catch (RuntimeException e) {
                //it's ok if file not found, because it may be deleted before transfer
                if (!e.getCause().getMessage().contains("FileNotFoundException")) {
                    log.info("Error while file processing", e.getMessage());
                    session.transfer(flowFileRef.get(), REL_FILE_FAILURE);
                } else session.remove(flowFile);
            }

            session.commit();
        }

        controller.markBatchReceived(batch);

        log.debug("Finish batch processing");
    }

    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        tryProcessInstructions(context, session);

        try {
            Batch batch = controller.getNextBatch(this.getIdentifier());
            if (batch == null) return;

            processBatch(session, batch);
        } catch (RuntimeException e) {
            getLogger().error(e.getMessage(), e);
        }
    }

    private void tryProcessInstructions(ProcessContext context, ProcessSession session) {
        getLogger().info("Start processing new instruction files...");

        final long pollingMillis = context.getProperty(INSTRUCTION_POLLING_INTERVAL).asTimePeriod(TimeUnit.MILLISECONDS);
        if (lastInstructionLookup + pollingMillis > System.currentTimeMillis()) return;

        lastInstructionLookup = System.currentTimeMillis();

        try {
            InstructionsContainer container = CGSON.fromJson(context.getProperty(INSTRUCTION_JSON).getValue(), InstructionsContainer.class);
            container.setNodeId(this.getIdentifier());

            controller.sendInstructions(container);
            getLogger().info("Finish processing new instruction files");
        } catch (Exception e) {
            getLogger().error("Error while sending instruction");
        }
    }
}
