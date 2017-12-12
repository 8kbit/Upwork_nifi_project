package com.thoughtapps.droppoint.core.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * Created by zaskanov on 04.04.2017.
 */

/**
 * Describe batch of files
 */
@Data
@Builder
@EqualsAndHashCode(of = {"id"})
public class Batch {
    Long id;
    String dropPointId;
    String nodeId;
    List<File> files;
    Boolean deleteOriginal;
    Boolean useCompression;
    Date createdDate;
}
