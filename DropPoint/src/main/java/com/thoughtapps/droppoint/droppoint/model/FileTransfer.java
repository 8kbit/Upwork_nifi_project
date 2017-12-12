package com.thoughtapps.droppoint.droppoint.model;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * Created by zaskanov on 04.04.2017.
 */

/**
 * Contains information about file marked as ready to send to drop point node
 */
@Data
@Builder
@Entity
public class FileTransfer {
    @Id
    @GeneratedValue(strategy = SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String filePath;

    @ManyToOne
    private FileTransferBatch fileTransferBatch;

    @Tolerate
    public FileTransfer() {
    }
}
