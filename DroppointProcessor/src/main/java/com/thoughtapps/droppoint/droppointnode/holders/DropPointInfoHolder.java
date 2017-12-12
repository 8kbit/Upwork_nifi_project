package com.thoughtapps.droppoint.droppointnode.holders;

import com.thoughtapps.droppoint.droppointnode.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zaskanov on 02.04.2017.
 */

/**
 * Hold information about all connected drop points
 */
@Component
public class DropPointInfoHolder {

    @Autowired
    Configuration configuration;

    private final Map<String, DropPointInfo> sshClientConfigMap = new HashMap<>();
    private final Map<String, Integer> idToPortCache = new HashMap<>();

    public synchronized DropPointInfo getInfo(String dropPointId) {
        return sshClientConfigMap.get(dropPointId);
    }

    public synchronized void putInfo(DropPointInfo info) {
        sshClientConfigMap.put(info.getDropPointId(), info);
        idToPortCache.put(info.getDropPointId(), info.getLocalPort());
    }

    public synchronized void removeInfo(String dropPointId) {
        sshClientConfigMap.remove(dropPointId);
        idToPortCache.remove(dropPointId);
    }

    public synchronized void clearInfo() {
        sshClientConfigMap.clear();
        idToPortCache.clear();
    }

    public synchronized Integer getDropPointPort(String dropPointId) {
        Integer port = idToPortCache.get(dropPointId);
        if (port != null) return port;

        for (int p = configuration.getMinDroppointPort(); p <= configuration.getMaxDroppointPort(); p++) {
            if (!idToPortCache.values().contains(p)) {
                idToPortCache.put(dropPointId, p);
                break;
            }
        }

        return idToPortCache.get(dropPointId);
    }

    public synchronized String findDropPointByPort(Integer port) {
        for (Map.Entry<String, Integer> entry : idToPortCache.entrySet())
            if (entry.getValue().equals(port)) return entry.getKey();

        return null;
    }
}
