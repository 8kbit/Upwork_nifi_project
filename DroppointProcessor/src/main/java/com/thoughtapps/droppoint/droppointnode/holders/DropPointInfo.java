package com.thoughtapps.droppoint.droppointnode.holders;

import lombok.Builder;
import lombok.Data;

/**
 * Created by zaskanov on 02.04.2017.
 */

/**
 * Contain information about drop point
 */
@Data
@Builder
public class DropPointInfo {
    private String dropPointId;
    private int localPort;
    private long lastPing = 0;
}
