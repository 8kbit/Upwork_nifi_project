package com.thoughtapps.droppoint.droppointnode.nifi;

import com.thoughtapps.droppoint.core.dto.Batch;
import com.thoughtapps.droppoint.core.dto.InstructionsContainer;
import com.thoughtapps.droppoint.core.messageExchange.highLevel.SshClient;
import org.apache.nifi.controller.ControllerService;

/**
 * Created by zaskanov on 29.04.2017.
 */
public interface DroppointController extends ControllerService {

    void sendInstructions(InstructionsContainer container) throws RuntimeException;

    void markBatchReceived(Batch batch) throws RuntimeException;

    void readFile(String path, String dropPointId, boolean deleteOriginal, boolean useCompression, SshClient.InputStreamCallback callback)
            throws RuntimeException;

    Batch getNextBatch(String nodeId) throws RuntimeException;
}
