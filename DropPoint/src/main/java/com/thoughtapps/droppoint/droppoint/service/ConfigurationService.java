package com.thoughtapps.droppoint.droppoint.service;

import com.thoughtapps.droppoint.droppoint.model.Configuration;
import com.thoughtapps.droppoint.droppoint.repositories.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by zaskanov on 04.04.2017.
 */

/**
 * Used to read and store configuration properties (DB)
 */
@Component
public class ConfigurationService {

    public final static String DROP_POINT_ID = "sftp.droppoint.id";
    public final static String DROP_POINT_ALLOWED_PROCESSOR_IDS = "sftp.droppoint.allowedProcessorIds";
    public final static String DROP_POINT_HOSTNAME = "sftp.droppoint.hostname";
    public final static String DROP_POINT_PORT = "sftp.droppoint.port";
    public final static String DROP_POINT_USERNAME = "sftp.droppoint.username";
    public final static String DROP_POINT_PASSWORD = "sftp.droppoint.password";
    public final static String DROP_POINT_ROOT_DIR = "sftp.droppoint.rootDir";
    public final static String DROP_POINT_PING_INTERVAL = "sftp.droppoint.pingIntervalSec";
    public final static String DROP_POINT_RECONNECTION_INTERVAL_SEC = "sftp.droppoint.reconnectionIntervalSec";

    public final static String NODE_HOSTNAME = "sftp.node.hostname";
    public final static String NODE_PORT = "sftp.node.port";
    public final static String NODE_USERNAME = "sftp.node.username";
    public final static String NODE_PASSWORD = "sftp.node.password";

    @Autowired
    ConfigurationRepository configurationRepository;

    @Transactional(readOnly = true)
    public String getPropertyValue(String key) {
        Configuration configuration = configurationRepository.findOneByKey(key);
        if (configuration == null) throw new RuntimeException("Property does not exists");

        return configuration.getValue();
    }

    public Integer getIntPropertyValue(String key) {
        return Integer.valueOf(getPropertyValue(key));
    }

    @Transactional
    public void setPropertyValue(String key, Object value) {
        Configuration configuration = configurationRepository.findOneByKey(key);
        if (configuration == null) throw new RuntimeException("Property does not exists");

        configuration.setValue(value != null ? value.toString() : null);
        configurationRepository.save(configuration);
    }

    @Transactional
    public Configuration createConfiguration(String key, Object value) {
        Configuration configuration = Configuration.builder().key(key).value(value != null ? value.toString() : null).build();
        return configurationRepository.save(configuration);
    }

    @Transactional(readOnly = true)
    public long count() {
        return configurationRepository.count();
    }
}
