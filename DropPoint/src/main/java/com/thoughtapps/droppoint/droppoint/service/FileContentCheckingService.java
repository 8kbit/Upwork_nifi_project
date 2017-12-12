package com.thoughtapps.droppoint.droppoint.service;

import java.io.File;

/**
 * Created by zaskanov on 13.04.2017.
 */

/**
 * Used to check if file content contains text snippet
 */
public interface FileContentCheckingService {
    boolean isFileContains(File file, String content);
}
