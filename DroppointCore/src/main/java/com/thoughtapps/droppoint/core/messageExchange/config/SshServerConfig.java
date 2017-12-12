package com.thoughtapps.droppoint.core.messageExchange.config;

import lombok.Builder;
import lombok.Data;

/**
 * Created by zaskanov on 02.04.2017.
 */

/**
 * Ssh server config
 */
@Data
@Builder
public class SshServerConfig {
    private String host;
    private int port;
    private String username;
    private String password;
    private boolean sftpEnabled;
    private String sftpRootDir;
}
