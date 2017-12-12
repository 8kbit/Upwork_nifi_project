package com.thoughtapps.droppoint.core.messageExchange.highLevel;

/**
 * Created by zaskanov on 16.04.2017.
 */
public interface SshServer extends AutoCloseable{

    public void init();

    public boolean isOpen();

    @Override
    void close() throws RuntimeException;

    public void setListener(SshServerListener listener);
}
