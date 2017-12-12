package com.thoughtapps.droppoint.droppoint.service;

import com.thoughtapps.droppoint.core.messageExchange.highLevel.SshClient;
import com.thoughtapps.droppoint.core.messageExchange.highLevel.SshClientImpl;
import com.thoughtapps.droppoint.core.messageExchange.config.SshClientConfig;
import org.springframework.stereotype.Component;

/**
 * Created by zaskanov on 06.04.2017.
 */
@Component
public class SshClientFactory {
    public SshClient createSshClient(SshClientConfig sshClientConfig) {
        return new SshClientImpl(sshClientConfig);
    }
}
