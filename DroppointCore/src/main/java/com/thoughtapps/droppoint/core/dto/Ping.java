package com.thoughtapps.droppoint.core.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Created by zaskanov on 02.04.2017.
 */

/**
 * Container for ping request
 */
@Data
@Builder
public class Ping {
    private String dropPointId;
    private BatchesContainer waitingBatches;
}
