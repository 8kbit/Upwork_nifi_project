package com.thoughtapps.droppoint.droppoint.schedule;

import com.thoughtapps.droppoint.core.dto.Instruction;
import com.thoughtapps.droppoint.core.dto.InstructionType;
import com.thoughtapps.droppoint.droppoint.model.Task;
import com.thoughtapps.droppoint.droppoint.repositories.TaskRepository;
import com.thoughtapps.droppoint.droppoint.task.TaskExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Created by zaskanov on 07.04.2017.
 */

/**
 * Periodically execute push file instructions
 */
@Slf4j
@Component
public class PreparePushFilesJob extends AbstractConfigScheduledJob {

    @Autowired
    TaskRepository taskRepository;

    @Qualifier("pullTaskExecutor")
    @Autowired
    TaskExecutor pullTaskExecutor;

    @Autowired
    private SshConnectionHolder sshConnectionHolder;

    @Override
    public void run() {
        if (!sshConnectionHolder.isConnectionAllowed()) return;

        log.info("Start {}", this.getClass().getSimpleName());

        try {
            List<Task> pushTasks = taskRepository.findByType(InstructionType.PUSH_FILE);
            long now = System.currentTimeMillis();
            for (Task task : pushTasks) {
                Instruction instruction = task.getInstruction();
                long lastFinished = task.getLastFinished() != null ? task.getLastFinished().getTime() : 0;

                if ((instruction.getPoolingIntervalSec() * 1000 + lastFinished) <= now) {
                    pullTaskExecutor.execute(task);
                    task.setLastFinished(new Date());
                    taskRepository.save(task);
                }
            }

            log.info("Finish {}", this.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("Error in {} : {}", this.getClass().getSimpleName(), e.getMessage());
        }
    }

    @Override
    protected int getIntervalSec() {
        return 2;
    }
}
