package com.thoughtapps.droppoint.droppointnode;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Created by zaskanov on 02.04.2017.
 */

/**
 * Hold application properties
 */
@Data
@Component
public class Configuration {

    public final static String NODE_ID = "sftp.node.id";
    public final static String HOST = "sftp.host";
    public final static String PORT = "sftp.port";
    public final static String USERNAME = "sftp.username";
    public final static String PASSWORD = "sftp.password";

    public final static String MIN_DROP_POINT_PORT = "sftp.minDropPointPort";
    public final static String MAX_DROP_POINT_PORT = "sftp.maxDropPointPort";
    public final static String NODE_DROP_POINT_ID_LIST = "sftp.node.dropPointIdList";

    public final static String DROP_POINT_USERNAME = "sftp.droppoint.username";
    public final static String DROP_POINT_PASSWORD = "sftp.droppoint.password";

    private String nodeId;
    private String host;
    private int port;
    private String username;
    private String password;
    private int minDroppointPort;
    private int maxDroppointPort;
    private Set<String> dropPointIds;

    private String dropPointUsername;
    private String dropPointPassword;

    @Deprecated
    private Map<String, Integer> clientToPort;
}
