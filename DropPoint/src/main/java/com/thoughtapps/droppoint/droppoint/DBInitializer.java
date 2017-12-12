package com.thoughtapps.droppoint.droppoint;

import com.thoughtapps.droppoint.droppoint.service.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.UUID;

/**
 * Created by zaskanov on 02.04.2017.
 */

/**
 * Used to initialize DB at first launch
 */
@Slf4j
@Component
public class DBInitializer {
    @Autowired
    private ConfigurationService confService;

    @Autowired
    private Environment env;

    private static final String INITIAL_CONF_PREFIX = "init.";

    @PostConstruct
    private void init() {
        if (confService.count() == 0) {
            log.info("Start first property initialization");

            try {
                initProperties();
            } catch (Exception e) {
                log.error("Failed to initialize properties", e);
            }

            log.info("Finish first property initialization");
        }
    }

    //save initial configuration values to DB
    @Transactional
    private void initProperties() {
        confService.createConfiguration(ConfigurationService.DROP_POINT_ID, UUID.randomUUID());
        confService.createConfiguration(ConfigurationService.DROP_POINT_ALLOWED_PROCESSOR_IDS, "");

        initProperty(ConfigurationService.DROP_POINT_HOSTNAME);
        initProperty(ConfigurationService.DROP_POINT_PORT);
        initProperty(ConfigurationService.DROP_POINT_USERNAME);
        initProperty(ConfigurationService.DROP_POINT_PASSWORD);
        initProperty(ConfigurationService.DROP_POINT_ROOT_DIR);
        initProperty(ConfigurationService.DROP_POINT_PING_INTERVAL);
        initProperty(ConfigurationService.DROP_POINT_RECONNECTION_INTERVAL_SEC);

        initProperty(ConfigurationService.NODE_HOSTNAME);
        initProperty(ConfigurationService.NODE_PORT);
        initProperty(ConfigurationService.NODE_USERNAME);
        initProperty(ConfigurationService.NODE_PASSWORD);
    }

    private void initProperty(String key) {
        confService.createConfiguration(key, env.getProperty(INITIAL_CONF_PREFIX + key));
    }
}
