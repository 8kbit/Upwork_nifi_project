package com.thoughtapps.droppoint.core.messageExchange.highLevel;

import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.util.net.SshdSocketAddress;

/**
 * Created by zaskanov on 16.04.2017.
 */

/**
 * listen for ssh server event such as connection open and start port forwarding
 */
public interface SshServerListener {
    public void sessionClosed(Session session);

    public void tunnelEstablished(Session session, SshdSocketAddress local, SshdSocketAddress remote);
}
