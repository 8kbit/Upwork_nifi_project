package com.thoughtapps.droppoint.droppoint.messageProcessors;

import com.thoughtapps.droppoint.core.dto.*;
import com.thoughtapps.droppoint.core.helpers.CGSON;
import com.thoughtapps.droppoint.core.messageExchange.highLevel.AbstractMessageProcessor;
import com.thoughtapps.droppoint.droppoint.model.Task;
import com.thoughtapps.droppoint.droppoint.repositories.TaskRepository;
import com.thoughtapps.droppoint.droppoint.service.ConfigurationService;
import com.thoughtapps.droppoint.droppoint.task.TaskExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created by zaskanov on 02.04.2017.
 */
@Slf4j
@Component
public class InstructionsProcessor extends AbstractMessageProcessor {
    @Autowired
    private ConfigurationService confService;

    @Autowired
    private TaskRepository taskRepository;

    @Qualifier("pullTaskExecutor")
    @Autowired
    private TaskExecutor pullTaskExecutor;

    @Qualifier("sqlTaskExecutor")
    @Autowired
    private TaskExecutor sqlTaskExecutor;

    /**
     * Process instructions received from drop point node
     *
     * @param request - request with instructions JSON
     * @return standard OK response
     */
    @Transactional
    @Override
    protected Message processInternal(Message request) {
        InstructionsContainer container = CGSON.fromJson(request.getPayloadJSON(), InstructionsContainer.class);

        Set<String> allowedProcessorIds = new HashSet<>(Arrays.asList(StringUtils.deleteWhitespace(
                confService.getPropertyValue(ConfigurationService.DROP_POINT_ALLOWED_PROCESSOR_IDS)).split(",")));
        if (!allowedProcessorIds.contains(container.getNodeId())) {
            log.info("Processor {} is not allowed to send instructions", container.getNodeId());
            return Message.ERROR;
        }

        //store new ping interval
        confService.setPropertyValue(ConfigurationService.DROP_POINT_PING_INTERVAL,
                container.getHeartbeatIntervalSec().toString());

        //drop old instructions
        taskRepository.delete(taskRepository.findByNodeId(container.getNodeId()));

        List<Task> taskList = saveTasks(container);

        String dropPointId = confService.getPropertyValue(ConfigurationService.DROP_POINT_ID);

        List<Batch> batches = new ArrayList<>();
        for (Task task : taskList) {
            //Pull file instructions processed immediately
            if (task.getType() == InstructionType.PULL_FILE) {
                List<Long> ids = pullTaskExecutor.execute(task);
                for (Long id : ids)
                    batches.add(Batch.builder().id(id).dropPointId(dropPointId).nodeId(task.getNodeId()).build());
            }
            //SQL instructions processed immediately
            if (task.getType() == InstructionType.SQL) {
                List<Long> ids = sqlTaskExecutor.execute(task);
                for (Long id : ids)
                    batches.add(Batch.builder().id(id).dropPointId(dropPointId).nodeId(task.getNodeId()).build());
            }
        }

        //Return OK response with batch ids (Pull file and SQL instructions)
        return Message.builder().type(MessageType.OK)
                .payloadJSON(CGSON.toJson(BatchesContainer.builder().batches(batches).build())).build();
    }

    private List<Task> saveTasks(InstructionsContainer container) {
        List<Task> taskList = new ArrayList<>();
        for (Instruction instruction : container.getInstructions()) {
            Task task = Task.builder().type(instruction.getType()).nodeId(container.getNodeId()).build();
            task.setInstruction(instruction);
            taskList.add(taskRepository.save(task));
        }

        return taskList;
    }
}
