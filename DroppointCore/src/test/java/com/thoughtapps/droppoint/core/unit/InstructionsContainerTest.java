package com.thoughtapps.droppoint.core.unit;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.testng.annotations.Test;
import com.thoughtapps.droppoint.core.dto.Instruction;
import com.thoughtapps.droppoint.core.dto.InstructionType;
import com.thoughtapps.droppoint.core.dto.InstructionsContainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.util.MatcherAssertionErrors.assertThat;


/**
 * Created by zaskanov on 01.04.2017.
 */
@Test
public class InstructionsContainerTest {

    @Test
    public void shouldParsePerson() throws IOException {
        InstructionsContainer container = new InstructionsContainer();
//        container.setDropPointId("Abc-123-xYz");
        container.setHeartbeatIntervalSec(180);

        List<Instruction> instructions = new ArrayList<>();
        container.setInstructions(instructions);

        Instruction pullFile = new Instruction();
        pullFile.setType(InstructionType.PULL_FILE);
        pullFile.setRuleName("Get accounting files");
        pullFile.setFilterDirPath("C://");
        pullFile.setFilterRecursionDepth(10);
        pullFile.setFilterFilename("example.txt");
        pullFile.setFilterIsIgnoreDottedFiles(true);
        pullFile.setFilterFileIncludeFileRegexList(Arrays.asList("^.*\\.(?!jpg$|png$)[^.]+$", "^.*\\.(?!jpg$|jpg$)[^.]+$"));
        pullFile.setFilterFileExcludeFileRegexList(Arrays.asList("^.*\\.(?!jpg$|doc$)[^.]+$", "^.*\\.(?!jpg$|exe$)[^.]+$"));
        pullFile.setFilterFileType("txt");
        pullFile.setFilterFileContent("example");
        pullFile.setFilterFilesPerBatch(10);
        pullFile.setFilterTotalFiles(500);
        pullFile.setFilterIsDeleteOriginal(true);
        pullFile.setFilterIsIgnoreProcessedFile(true);
        instructions.add(pullFile);

        Instruction pushFile = new Instruction();
        pushFile.setType(InstructionType.PUSH_FILE);
        pushFile.setRuleName("Keep sending new admin files");
        pushFile.setPoolingIntervalSec(180);
        pushFile.setIsPushRecursively(true);
        pushFile.setIsUseCompression(true);
        pushFile.setIsUseNaturalOrdering(true);
        pushFile.setFilterDirPath("C://");
        pushFile.setFilterRecursionDepth(10);
        pushFile.setFilterFilename("example.txt");
        pushFile.setFilterIsIgnoreDottedFiles(true);
        pushFile.setFilterFileIncludeFileRegexList(Arrays.asList("^.*\\.(?!jpg$|png$)[^.]+$", "^.*\\.(?!jpg$|jpg$)[^.]+$"));
        pushFile.setFilterFileExcludeFileRegexList(Arrays.asList("^.*\\.(?!jpg$|doc$)[^.]+$", "^.*\\.(?!jpg$|exe$)[^.]+$"));
        pushFile.setFilterFileType("txt");
        pushFile.setFilterFileContent("example");
        pushFile.setFilterFilesPerBatch(10);
        pushFile.setFilterTotalFiles(500);
        pushFile.setFilterIsDeleteOriginal(true);
        pushFile.setFilterIsIgnoreProcessedFile(true);
        instructions.add(pushFile);

        Instruction sqlFile = new Instruction();
        sqlFile.setType(InstructionType.SQL);
        sqlFile.setRuleName("Get HR data from DB");
        sqlFile.setIsUseCompression(true);
        sqlFile.setDbConnectionURL("225.225.278.144:2554");
        sqlFile.setDbConnectionDriverClassName("com.mysql.jdbc.Driver");
        sqlFile.setDbConnectionDriverLocations(Arrays.asList("Server/sqljdbc_4.0/enu/sqljdbc4.jar", "C:/Oracle/product/11.2.0/Db_1/jdbc/lib/ojdbc5.jar"));
        sqlFile.setDbUser("#Abcd1234");
        sqlFile.setDbPassword("xxxxxxx");
        sqlFile.setDbSQLQuery("select * from hr_table");
        instructions.add(sqlFile);

        Gson gson = new Gson();
        InstructionsContainer template = gson.fromJson(IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("InstructionsRequest.json")), InstructionsContainer.class);
        assertThat(template, new ReflectionEquals(container));
    }
}
