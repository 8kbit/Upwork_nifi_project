package com.thoughtapps.droppoint.core.messageExchange.config;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

/**
 * Created by zaskanov on 02.04.2017.
 */

/**
 * Ssh client config
 */
@Data
@Builder
public class SshClientConfig {
    private String host;
    private int port;
    private String username;
    private String password;
    private Boolean useCompression = false;

    @Tolerate
    public SshClientConfig() {
    }
}
