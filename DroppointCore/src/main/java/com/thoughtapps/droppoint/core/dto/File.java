package com.thoughtapps.droppoint.core.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Created by zaskanov on 05.04.2017.
 */

/**
 * Describe file ready to transfer
 */
@Data
@Builder
public class File {
    String filePath;
}
