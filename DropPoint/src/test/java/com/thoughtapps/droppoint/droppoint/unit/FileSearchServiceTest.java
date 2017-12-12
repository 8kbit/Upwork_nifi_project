package com.thoughtapps.droppoint.droppoint.unit;

import com.thoughtapps.droppoint.core.dto.Instruction;
import com.thoughtapps.droppoint.droppoint.service.FileSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Created by zaskanov on 07.04.2017.
 */
@Test
public class FileSearchServiceTest extends AbstractTransactionalTest {

    @Autowired
    FileSearchService fileSearchService;

    @Test
    public void testFileDirPath() {
        Instruction instruction = new Instruction();
        instruction.setFilterDirPath("1level/2level/3level");

        File root = getSFTPRoot();
        List<String> files = setSystemFileSeparator(fileSearchService.findFiles(instruction, root.getAbsolutePath()));
        List<String> expected = Arrays.asList(Paths.get("1level/2level/3level/example.txt").toString());
        assertEquals(expected, files);
    }

    @Test
    public void testRecursionDepthButNotRecursively() {
        Instruction instruction = new Instruction();
        instruction.setFilterRecursionDepth(3);

        File root = getSFTPRoot();
        Set<String> files = new HashSet<>(setSystemFileSeparator(fileSearchService.findFiles(instruction, root.getAbsolutePath())));
        Set<String> expected = new HashSet<>(Arrays.asList("candidates.txt", "FinanceReport.csv", ".dotted.txt"));
        assertEquals(expected, files);
    }

    @Test
    public void testRecursionDepth() {
        Instruction instruction = new Instruction();
        instruction.setFilterRecursionDepth(2);
        instruction.setIsPushRecursively(true);

        File root = getSFTPRoot();
        Set<String> files = new HashSet<>(setSystemFileSeparator(fileSearchService.findFiles(instruction, root.getAbsolutePath())));
        Set<String> expected = new HashSet<>(Arrays.asList("candidates.txt", "FinanceReport.csv", ".dotted.txt",
                Paths.get("1level", "notes.xml").toString(),
                Paths.get("1level", "2level", "car.jpg").toString()));
        assertEquals(expected, files);
    }

    @Test
    public void testFileName() {
        Instruction instruction = new Instruction();
        instruction.setFilterRecursionDepth(3);
        instruction.setIsPushRecursively(true);
        instruction.setFilterFilename("example.txt");

        File root = getSFTPRoot();
        Set<String> files = new HashSet<>(setSystemFileSeparator(fileSearchService.findFiles(instruction, root.getAbsolutePath())));
        Set<String> expected = new HashSet<>(Arrays.asList(Paths.get("1level", "2level", "3level", "example.txt").toString()));
        assertEquals(expected, files);
    }

    @Test
    public void testIgnoreDottedFiles() {
        Instruction instruction = new Instruction();
        instruction.setFilterRecursionDepth(2);
        instruction.setIsPushRecursively(true);
        instruction.setFilterIsIgnoreDottedFiles(true);

        File root = getSFTPRoot();
        Set<String> files = new HashSet<>(setSystemFileSeparator(fileSearchService.findFiles(instruction, root.getAbsolutePath())));
        Set<String> expected = new HashSet<>(Arrays.asList("candidates.txt", "FinanceReport.csv",
                Paths.get("1level", "notes.xml").toString(),
                Paths.get("1level", "2level", "car.jpg").toString()));
        assertEquals(expected, files);
    }

    @Test
    public void testFileType() {
        Instruction instruction = new Instruction();
        instruction.setFilterRecursionDepth(3);
        instruction.setIsPushRecursively(true);
        instruction.setFilterFileType("txt");

        File root = getSFTPRoot();
        Set<String> files = new HashSet<>(setSystemFileSeparator(fileSearchService.findFiles(instruction, root.getAbsolutePath())));
        Set<String> expected = new HashSet<>(Arrays.asList("candidates.txt", ".dotted.txt",
                Paths.get("1level", "2level", "3level", "example.txt").toString()));
        assertEquals(expected, files);
    }

    @Test
    public void testFileContent() {
        Instruction instruction = new Instruction();
        instruction.setFilterRecursionDepth(3);
        instruction.setIsPushRecursively(true);
        instruction.setFilterFileContent("Lorem Ipsum");

        File root = getSFTPRoot();
        Set<String> files = new HashSet<>(setSystemFileSeparator(fileSearchService.findFiles(instruction, root.getAbsolutePath())));
        Set<String> expected = new HashSet<>(Arrays.asList(
                Paths.get("1level", "2level", "3level", "example.txt").toString()));
        assertEquals(expected, files);
    }

    @Test
    public void testIncludeFileRegex() {
        Instruction instruction = new Instruction();
        instruction.setFilterRecursionDepth(3);
        instruction.setIsPushRecursively(true);
        instruction.setFilterFileIncludeFileRegexList(Arrays.asList("^.*\\.(txt$)", "^.*\\.(xml$)"));

        File root = getSFTPRoot();
        Set<String> files = new HashSet<>(setSystemFileSeparator(fileSearchService.findFiles(instruction, root.getAbsolutePath())));
        Set<String> expected = new HashSet<>(Arrays.asList("candidates.txt", ".dotted.txt",
                Paths.get("1level", "notes.xml").toString(),
                Paths.get("1level", "2level", "3level", "example.txt").toString()));
        assertEquals(expected, files);
    }

    @Test
    public void testExcludeFileRegex() {
        Instruction instruction = new Instruction();
        instruction.setFilterRecursionDepth(3);
        instruction.setIsPushRecursively(true);
        instruction.setFilterFileExcludeFileRegexList(Arrays.asList("^.*\\.(txt$)", "^.*\\.(xml$)"));

        File root = getSFTPRoot();
        Set<String> files = new HashSet<>(setSystemFileSeparator(fileSearchService.findFiles(instruction, root.getAbsolutePath())));
        Set<String> expected = new HashSet<>(Arrays.asList("FinanceReport.csv",
                Paths.get("1level", "2level", "car.jpg").toString()));
        assertEquals(expected, files);
    }

    @Test
    public void testIncludeExcludeFileRegex() {
        Instruction instruction = new Instruction();
        instruction.setFilterRecursionDepth(3);
        instruction.setIsPushRecursively(true);
        instruction.setFilterFileIncludeFileRegexList(Arrays.asList("^.*\\.(csv$)"));
        instruction.setFilterFileExcludeFileRegexList(Arrays.asList("^.*\\.(txt$)", "^.*\\.(xml$)"));

        File root = getSFTPRoot();
        Set<String> files = new HashSet<>(setSystemFileSeparator(fileSearchService.findFiles(instruction, root.getAbsolutePath())));
        Set<String> expected = new HashSet<>(Arrays.asList("FinanceReport.csv"));
        assertEquals(expected, files);
    }

    private File getSFTPRoot() {
        try {
            return new File(this.getClass().getClassLoader().getResource("ftpRoot").toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<String> setSystemFileSeparator(Set<String> oldPaths) {
        Set<String> newPaths = new HashSet<>();
        for (String oldPath : oldPaths) {
            newPaths.add(Paths.get(oldPath).toString());
        }
        return newPaths;
    }

    private List<String> setSystemFileSeparator(List<String> oldPaths) {
        List<String> newPaths = new ArrayList<>();
        for (String oldPath : oldPaths) {
            newPaths.add(Paths.get(oldPath).toString());
        }
        return newPaths;
    }
}
