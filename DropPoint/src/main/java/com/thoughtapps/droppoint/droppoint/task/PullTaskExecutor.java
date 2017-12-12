package com.thoughtapps.droppoint.droppoint.task;

import com.google.common.collect.Lists;
import com.thoughtapps.droppoint.core.dto.Instruction;
import com.thoughtapps.droppoint.droppoint.model.FileTransfer;
import com.thoughtapps.droppoint.droppoint.model.FileTransferBatch;
import com.thoughtapps.droppoint.droppoint.model.Task;
import com.thoughtapps.droppoint.droppoint.model.TransferStatus;
import com.thoughtapps.droppoint.droppoint.repositories.FileTransferBatchRepository;
import com.thoughtapps.droppoint.droppoint.repositories.TaskRepository;
import com.thoughtapps.droppoint.droppoint.service.ConfigurationService;
import com.thoughtapps.droppoint.droppoint.service.FileSearchService;
import com.thoughtapps.droppoint.droppoint.service.FileTransferService;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by zaskanov on 04.04.2017.
 */

/**
 * Executes pull file instruction
 */
@Component("pullTaskExecutor")
public class PullTaskExecutor implements TaskExecutor {

    @Autowired
    private ConfigurationService confService;

    @Autowired
    private FileTransferBatchRepository ftbRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private FileTransferService ftService;

    @Autowired
    private FileSearchService fileSearchService;

    @Transactional
    @Override
    public List<Long> execute(Task task) {
        task.setLastFinished(new Date());
        taskRepository.save(task);
        Instruction instruction = task.getInstruction();

        //search for files according to instruction conditions
        String rootDir = confService.getPropertyValue(ConfigurationService.DROP_POINT_ROOT_DIR);
        List<String> files = fileSearchService.findFiles(instruction, rootDir);
        if (instruction.getFilterTotalFiles() != null)
            files = files.subList(0, Math.min(files.size(), instruction.getFilterTotalFiles()));
        if (instruction.getFilterIsIgnoreProcessedFile())
            files = ftService.filterTransferredFiles(files, task.getNodeId());


        //mark files as ready to send to drop point processor
        if (!files.isEmpty()) {
            List<Long> ftbIds = new ArrayList<>();
            int filesPerBatch = (Integer) ObjectUtils.defaultIfNull(instruction.getFilterFilesPerBatch(), 10);
            List<List<String>> fileLists = Lists.partition((List) files, filesPerBatch);
            for (List<String> fileList : fileLists) {
                FileTransferBatch ftb = FileTransferBatch.builder().status(TransferStatus.WAITING)
                        .useCompression(BooleanUtils.isTrue(instruction.getIsUseCompression()))
                        .deleteOriginal(BooleanUtils.isTrue(instruction.getFilterIsDeleteOriginal()))
                        .createdDate(new Date()).fileTransfers(new ArrayList<>(files.size()))
                        .nodeId(task.getNodeId())
                        .build();

                for (String file : fileList) {
                    FileTransfer ft = FileTransfer.builder().filePath(file).fileTransferBatch(ftb).build();
                    ftb.getFileTransfers().add(ft);
                }

                ftbIds.add(ftbRepository.save(ftb).getId());
            }
            return ftbIds;
        } else return Collections.emptyList();
    }
}
