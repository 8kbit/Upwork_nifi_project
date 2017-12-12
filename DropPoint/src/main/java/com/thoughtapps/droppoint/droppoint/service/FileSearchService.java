package com.thoughtapps.droppoint.droppoint.service;

import com.thoughtapps.droppoint.core.dto.Instruction;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by zaskanov on 08.04.2017.
 */

/**
 * Search files according to specified rules
 */
@Service
public class FileSearchService {

    @Autowired
    FileContentCheckingService simpleFileContentCheckingService;

    public static final String EXT_TXT = "txt";
    public static final String EXT_CSV = "csv";
    public static final String EXT_XML = "xml";

    private Map<String, FileContentCheckingService> contentCheckers;

    @PostConstruct
    private void init() {
        contentCheckers = new HashMap<>();
        contentCheckers.put(EXT_TXT, simpleFileContentCheckingService);
        contentCheckers.put(EXT_CSV, simpleFileContentCheckingService);
        contentCheckers.put(EXT_XML, simpleFileContentCheckingService);
    }

    public List<String> findFiles(Instruction instruction, String rootDir) {
        File realRoot = new File(rootDir);
        if (StringUtils.isNotBlank(instruction.getFilterDirPath())) {
            realRoot = new File(realRoot, instruction.getFilterDirPath());
            if (!realRoot.exists()) return Collections.emptyList();
        }

        List<Pattern> includePatterns = compilePatterns(instruction.getFilterFileIncludeFileRegexList());
        List<Pattern> excludePatterns = compilePatterns(instruction.getFilterFileExcludeFileRegexList());

        List<String> fileNames = visitFolder(realRoot, instruction, 0, includePatterns, excludePatterns);

        Path rootDirPath = Paths.get(rootDir).toAbsolutePath();
        for (int i = 0; i < fileNames.size(); i++) {
            String relativePath = rootDirPath.relativize(Paths.get(fileNames.get(i))).toString();
            fileNames.set(i, relativePath.replace("\\", "/"));
        }

        return fileNames;
    }

    private List<String> visitFolder(File root, Instruction instruction, int directoryLevel,
                                     List<Pattern> includePatterns, List<Pattern> excludePatterns) {
        if (!instruction.getIsPushRecursively() && directoryLevel > 0) return Collections.emptyList();

        if (instruction.getFilterRecursionDepth() != null && directoryLevel > instruction.getFilterRecursionDepth())
            return Collections.emptyList();

        List<File> filesToVisit = new ArrayList<>(Arrays.asList(root.listFiles()));
        if (instruction.getIsUseNaturalOrdering()) sortFilesNatural(filesToVisit);

        List<File> dirs = new ArrayList<>();
        List<String> files = new ArrayList<>();

        for (File file : filesToVisit) {
            if (file.isFile()) {
                if (StringUtils.isNotBlank(instruction.getFilterFilename()) &&
                        !StringUtils.equalsIgnoreCase(file.getName(), instruction.getFilterFilename()))
                    continue;

                if (instruction.getFilterIsIgnoreDottedFiles() && file.getName().startsWith(".")) continue;

                if (StringUtils.isNotBlank(instruction.getFilterFileType()) &&
                        !StringUtils.equalsIgnoreCase(FilenameUtils.getExtension(file.getName()), instruction.getFilterFileType()))
                    continue;

                if (!isSatisfyPatterns(file.getName(), includePatterns, excludePatterns)) continue;

                if (StringUtils.isNotBlank(instruction.getFilterFileContent()) && !isFileContains(file, instruction.getFilterFileContent()))
                    continue;

                files.add(file.getAbsolutePath());
            } else dirs.add(file);
        }

        if (!dirs.isEmpty()) {
            for (File dir : dirs) {
                files.addAll(visitFolder(dir, instruction, directoryLevel + 1, includePatterns, excludePatterns));
            }
        }

        return files;
    }

    /**
     * Check if file name meets the specified requirements
     * @param fileName - file name
     * @param includePatterns - if not empty file name must satisfy one of this patterns
     * @param excludePatterns - used only if includePatterns is empty
     */
    private boolean isSatisfyPatterns(String fileName, List<Pattern> includePatterns, List<Pattern> excludePatterns) {
        if (CollectionUtils.isNotEmpty(includePatterns)) {
            for (Pattern pattern : includePatterns) {
                if (pattern.matcher(fileName).matches()) {
                    return true;
                }
            }

            return false;
        }
        if (CollectionUtils.isNotEmpty(excludePatterns)) {
            for (Pattern pattern : excludePatterns) {
                if (pattern.matcher(fileName).matches()) {
                    return false;
                }
            }
        }

        return true;
    }

    //Check if file contains following text snippet
    private boolean isFileContains(File file, String content) {
        String ext = StringUtils.lowerCase(FilenameUtils.getExtension(file.getName()));
        FileContentCheckingService checkingService = contentCheckers.get(StringUtils.lowerCase(ext));

        if (checkingService == null) return false;

        return checkingService.isFileContains(file, content);
    }

    private List<Pattern> compilePatterns(List<String> strings) {
        if (CollectionUtils.isEmpty(strings)) return Collections.emptyList();

        List<Pattern> patterns = new ArrayList<>(strings.size());
        for (String s : strings)
            patterns.add(Pattern.compile(s));

        return patterns;
    }

    private void sortFilesNatural(List<File> files) {
        files.sort(new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }
}
