package com.thoughtapps.droppoint.droppoint.helpers;

import com.thoughtapps.droppoint.core.messageExchange.config.SshClientConfig;
import com.thoughtapps.droppoint.core.messageExchange.config.SshServerConfig;
import com.thoughtapps.droppoint.droppoint.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by zaskanov on 02.04.2017.
 */

/**
 * Helper for ssh server and ssh client config creation
 */
@Component
public class SshConfigHelper {

    @Autowired
    private ConfigurationService configurationService;

    public SshServerConfig createSshServerConfig() {
        return SshServerConfig.builder().host(configurationService.getPropertyValue(ConfigurationService.DROP_POINT_HOSTNAME))
                .port(Integer.valueOf(configurationService.getPropertyValue(ConfigurationService.DROP_POINT_PORT)))
                .username(configurationService.getPropertyValue(ConfigurationService.DROP_POINT_USERNAME))
                .password(configurationService.getPropertyValue(ConfigurationService.DROP_POINT_PASSWORD))
                .sftpRootDir(configurationService.getPropertyValue(ConfigurationService.DROP_POINT_ROOT_DIR))
                .sftpEnabled(true).build();
    }

    public SshClientConfig createSshClientConfig() {
        return SshClientConfig.builder().host(configurationService.getPropertyValue(ConfigurationService.NODE_HOSTNAME))
                .port(Integer.valueOf(configurationService.getPropertyValue(ConfigurationService.NODE_PORT)))
                .username(configurationService.getPropertyValue(ConfigurationService.NODE_USERNAME))
                .password(configurationService.getPropertyValue(ConfigurationService.NODE_PASSWORD))
                .build();
    }
}
