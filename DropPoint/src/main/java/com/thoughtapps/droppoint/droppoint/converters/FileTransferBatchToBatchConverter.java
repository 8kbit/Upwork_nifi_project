package com.thoughtapps.droppoint.droppoint.converters;

import com.thoughtapps.droppoint.core.dto.Batch;
import com.thoughtapps.droppoint.core.dto.File;
import com.thoughtapps.droppoint.droppoint.model.FileTransfer;
import com.thoughtapps.droppoint.droppoint.model.FileTransferBatch;
import com.thoughtapps.droppoint.droppoint.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Created by zaskanov on 05.04.2017.
 */
@Component
public class FileTransferBatchToBatchConverter {

    @Autowired
    ConfigurationService confService;

    public Batch convert(FileTransferBatch ftb, boolean onlyIds) {
        Batch batch;

        if (onlyIds) batch = Batch.builder().id(ftb.getId())
                .dropPointId(confService.getPropertyValue(ConfigurationService.DROP_POINT_ID)).nodeId(ftb.getNodeId())
                .build();
        else {
            batch = Batch.builder().id(ftb.getId()).deleteOriginal(ftb.getDeleteOriginal())
                    .useCompression(ftb.getUseCompression())
                    .createdDate(ftb.getCreatedDate()).files(new ArrayList<>())
                    .dropPointId(confService.getPropertyValue(ConfigurationService.DROP_POINT_ID))
                    .nodeId(ftb.getNodeId()).build();
            for (FileTransfer transfer : ftb.getFileTransfers()) {
                batch.getFiles().add(File.builder().filePath(transfer.getFilePath()).build());
            }
        }

        return batch;
    }
}
