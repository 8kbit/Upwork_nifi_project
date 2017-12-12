package com.thoughtapps.droppoint.droppoint.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zaskanov on 07.04.2017.
 */
@Service
public class FileTransferService {

    @Autowired
    EntityManager entityManager;

    NamedParameterJdbcTemplate jdbcTemplate;

    private final String NEW_FILEPATH_SQL = "select distinct ft.filepath from filetransfer ft " +
            "join filetransferbatch ftb on ft.filetransferbatch_id=ftb.id " +
            "where filepath in ( :paths ) and ftb.nodeid = :nodeId";

    @Autowired
    private void setJdbcTemplate(DataSource dataSource) {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    //Retain only files that does not yet sent to drop point processor
    @Transactional(readOnly = true)
    public List<String> filterTransferredFiles(List<String> paths, String nodeId) {
        entityManager.flush();
        Map<String, Object> params = new HashMap<>();
        params.put("paths", paths);
        params.put("nodeId", nodeId);
        List<String> existingPaths =
                jdbcTemplate.queryForList(NEW_FILEPATH_SQL, params, String.class);
        List<String> newPaths = new ArrayList<>(paths);
        newPaths.removeAll(existingPaths);
        return newPaths;
    }
}
