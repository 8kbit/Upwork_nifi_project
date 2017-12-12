package com.thoughtapps.droppoint.droppoint.model;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * Created by zaskanov on 04.04.2017.
 */

/**
 * All files grouped in batches. Drop point node process all files from batch at once
 */
@Data
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
public class FileTransferBatch {
    @Id
    @GeneratedValue(strategy = SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private TransferStatus status;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "fileTransferBatch")
    private List<FileTransfer> fileTransfers;

    @Column(nullable = false)
    private Boolean deleteOriginal;

    @Column(nullable = false)
    private Boolean useCompression;

    @CreatedDate
    @Column(nullable = false)
    private Date createdDate;

    @Column(nullable = false)
    private String nodeId;

    @Tolerate
    public FileTransferBatch() {
    }
}
