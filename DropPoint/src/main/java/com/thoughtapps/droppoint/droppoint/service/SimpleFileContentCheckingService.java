package com.thoughtapps.droppoint.droppoint.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * Created by zaskanov on 13.04.2017.
 */

/**
 * Check if file contains text snippet. Only for simple text files (txt, csv, xml)
 */
@Slf4j
@Component
public class SimpleFileContentCheckingService implements FileContentCheckingService {
    @Override
    public boolean isFileContains(File file, String content) {
        try {
            String fileContent = StringUtils.lowerCase(FileUtils.readFileToString(file, Charsets.UTF_8.name()));

            return fileContent.contains(StringUtils.lowerCase(content));
        } catch (IOException e) {
            log.error("Error while file content checking", e);
            throw new RuntimeException(e);
        }
    }
}
