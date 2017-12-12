package com.thoughtapps.droppoint.droppoint.unit;

import com.thoughtapps.droppoint.core.dto.Instruction;
import com.thoughtapps.droppoint.core.dto.InstructionType;
import com.thoughtapps.droppoint.droppoint.model.FileTransferBatch;
import com.thoughtapps.droppoint.droppoint.model.Task;
import com.thoughtapps.droppoint.droppoint.repositories.FileTransferBatchRepository;
import com.thoughtapps.droppoint.droppoint.repositories.TaskRepository;
import com.thoughtapps.droppoint.droppoint.service.ConfigurationService;
import com.thoughtapps.droppoint.droppoint.task.TaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * Created by zaskanov on 01.04.2017.
 */
@Test
public class SQLTaskExecutorTest extends AbstractTransactionalTest {

    @Autowired
    @Qualifier("sqlTaskExecutor")
    TaskExecutor sqlTaskExecutor;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    FileTransferBatchRepository ftbRepository;

    @Autowired
    ConfigurationService confService;

    @Test
    public void testBatchInfo() throws URISyntaxException {
        confService.setPropertyValue(ConfigurationService.DROP_POINT_ROOT_DIR,
                confService.getPropertyValue(ConfigurationService.DROP_POINT_ROOT_DIR).replace("/ftpRoot", ""));

        Instruction instruction = new Instruction();
        instruction.setRuleName("test");
        instruction.setDbUser("admin");
        instruction.setDbPassword("admin");
        instruction.setDbConnectionDriverClassName("org.hsqldb.jdbc.JDBCDriver");
        instruction.setDbSQLQuery("SELECT * FROM   INFORMATION_SCHEMA.TABLES");
        instruction.setDbConnectionURL("jdbc:hsqldb:mem:mymemdb");
        instruction.setDbConnectionDriverLocations(Arrays.asList(
                this.getClass().getClassLoader().getResource("jdbc/hsqldb-2.4.0.jar").toURI().getPath(),
                "not existing path"
        ));

        Task task = Task.builder().type(InstructionType.SQL).nodeId("123").build();
        task.setInstruction(instruction);
        taskRepository.save(task);

        List<Long> batchIds = sqlTaskExecutor.execute(task);
        assertEquals(batchIds.size(), 1);

        FileTransferBatch ftb = ftbRepository.findOne(batchIds.get(0));
        assertEquals(ftb.getFileTransfers().size(), 1);
    }
}
