package com.thoughtapps.droppoint.droppointnode.helpers;

import com.thoughtapps.droppoint.core.messageExchange.config.SshClientConfig;
import com.thoughtapps.droppoint.core.messageExchange.config.SshServerConfig;
import com.thoughtapps.droppoint.droppointnode.Configuration;
import com.thoughtapps.droppoint.droppointnode.holders.DropPointInfoHolder;
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
    private Configuration configuration;

    @Autowired
    private DropPointInfoHolder dropPointInfoHolder;

    public SshServerConfig createSshServerConfig() {
        return SshServerConfig.builder().host(configuration.getHost()).port(configuration.getPort())
                .username(configuration.getUsername()).password(configuration.getPassword())
                .sftpEnabled(false).build();
    }

    public SshClientConfig createSshClientConfig(String dropPointId) {
        return SshClientConfig.builder().host("127.0.0.1").port(dropPointInfoHolder.getDropPointPort(dropPointId))
                .username(configuration.getDropPointUsername()).password(configuration.getDropPointPassword())
                .build();
    }
}
