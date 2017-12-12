package com.thoughtapps.droppoint.droppoint.task;

import com.thoughtapps.droppoint.core.dto.Instruction;
import com.thoughtapps.droppoint.core.helpers.CGSON;
import com.thoughtapps.droppoint.droppoint.model.FileTransfer;
import com.thoughtapps.droppoint.droppoint.model.FileTransferBatch;
import com.thoughtapps.droppoint.droppoint.model.Task;
import com.thoughtapps.droppoint.droppoint.model.TransferStatus;
import com.thoughtapps.droppoint.droppoint.repositories.FileTransferBatchRepository;
import com.thoughtapps.droppoint.droppoint.repositories.TaskRepository;
import com.thoughtapps.droppoint.droppoint.service.ConfigurationService;
import com.thoughtapps.droppoint.droppoint.service.FileTransferService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Created by zaskanov on 04.04.2017.
 */

/**
 * Execute SQL instruction
 */
@Slf4j
@Component("sqlTaskExecutor")
public class SQLTaskExecutor implements TaskExecutor {

    @Autowired
    private ConfigurationService confService;

    @Autowired
    private FileTransferBatchRepository ftbRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private FileTransferService ftService;

    @Transactional
    @Override
    public List<Long> execute(Task task) {
        task.setLastFinished(new Date());
        taskRepository.save(task);

        Instruction instruction = task.getInstruction();

        try {
            //load jdbc driver
            for (String path : instruction.getDbConnectionDriverLocations())
                tryLoadDriver(path, instruction.getDbConnectionDriverClassName());

            //execute SQL
            List<Map<String, Object>> selectResult = executeSelect(instruction);

            //save SQL results to temp folder
            String fileName = Paths.get(confService.getPropertyValue(ConfigurationService.DROP_POINT_ROOT_DIR),
                    "tmp",
                    (StringUtils.deleteWhitespace(instruction.getRuleName()) + "_" +
                            System.currentTimeMillis() + ".json")).toString();
            File file = new File(fileName);
            file.getParentFile().mkdirs();
            FileUtils.writeStringToFile(file, CGSON.toJson(selectResult), Charsets.UTF_8);

            //mark file as ready to transfer to drop point processor
            Path rootDirPath = Paths.get(confService.getPropertyValue(ConfigurationService.DROP_POINT_ROOT_DIR)).toAbsolutePath();
            fileName = rootDirPath.relativize(Paths.get(file.getAbsolutePath())).toString().replace("\\", "/");

            FileTransferBatch ftb = FileTransferBatch.builder().status(TransferStatus.WAITING)
                    .useCompression(BooleanUtils.isTrue(instruction.getIsUseCompression()))
                    .deleteOriginal(true)
                    .createdDate(new Date()).fileTransfers(new ArrayList<>(1))
                    .nodeId(task.getNodeId()).build();

            FileTransfer ft = FileTransfer.builder().filePath(fileName).fileTransferBatch(ftb).build();
            ftb.getFileTransfers().add(ft);

            ftb = ftbRepository.save(ftb);
            return Collections.singletonList(ftb.getId());
        } catch (Exception e) {
            log.error("Error while executing SQL task", e);
            return Collections.emptyList();
        }
    }

    /**
     * Load jdbc driver from jar
     *
     * @param pathToJar - path to jar. It is ok if jar does not exist
     * @param className - jdbc driver class name
     */
    private void tryLoadDriver(String pathToJar, String className) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            File jarFile = new File(pathToJar);
            if (jarFile.exists())
                try {
                    // Use reflection
                    Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
                    method.setAccessible(true);
                    URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
                    method.invoke(classLoader, new Object[]{jarFile.toURL()});

                    Class clazz = Class.forName(className);
                    Driver driver = (Driver) clazz.newInstance();
                    DriverManager.registerDriver(driver);
                } catch (Exception e2) {
                    log.error("Failed to load class {} from jar {}", className, pathToJar);
                }
        }
    }

    //Execute instruction SQL and fetch result
    private List<Map<String, Object>> executeSelect(Instruction instruction) throws SQLException {
        Properties connectionProps = new Properties();
        connectionProps.put("user", instruction.getDbUser());
        connectionProps.put("password", instruction.getDbPassword());
        try (Connection conn = DriverManager.getConnection(instruction.getDbConnectionURL(), connectionProps);
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(instruction.getDbSQLQuery());
            List<Map<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    row.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
                }
                rows.add(row);
            }
            return rows;
        } catch (SQLException e) {
            log.error("Failed to execute select");
            throw e;
        }
    }
}
