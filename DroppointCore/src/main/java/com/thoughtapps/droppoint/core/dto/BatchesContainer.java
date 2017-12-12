package com.thoughtapps.droppoint.core.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Created by zaskanov on 04.04.2017.
 */

/**
 * Container for batches
 */
@Data
@Builder
public class BatchesContainer {
    List<Batch> batches;
}
