package com.thoughtapps.droppoint.core.messageExchange.highLevel;

import com.thoughtapps.droppoint.core.dto.Message;

import java.io.InputStream;

/**
 * Created by zaskanov on 16.04.2017.
 */
public interface SshClient extends AutoCloseable {
    public void init();

    @Override
    void close() throws RuntimeException;

    public boolean isOpen();

    public Message sendMessage(Message message);

    public void readFile(String path, SshClientImpl.InputStreamCallback callback);

    public void removeFile(String path);

    public void forwardRemotePort(String remoteHost, int remotePort, String localHost, int localPort);

    public abstract static class InputStreamCallback {
        public abstract void read(InputStream in);
    }
}
