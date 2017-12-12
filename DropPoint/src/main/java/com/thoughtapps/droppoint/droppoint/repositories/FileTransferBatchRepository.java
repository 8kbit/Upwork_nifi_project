package com.thoughtapps.droppoint.droppoint.repositories;

import com.thoughtapps.droppoint.droppoint.model.FileTransferBatch;
import com.thoughtapps.droppoint.droppoint.model.TransferStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by zaskanov on 04.04.2017.
 */
@Repository
public interface FileTransferBatchRepository extends CrudRepository<FileTransferBatch, Long> {

    List<FileTransferBatch> findByStatusOrderByCreatedDateAsc(TransferStatus status);
}
