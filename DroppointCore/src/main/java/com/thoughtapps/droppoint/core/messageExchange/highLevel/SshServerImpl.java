package com.thoughtapps.droppoint.core.messageExchange.highLevel;

import com.thoughtapps.droppoint.core.dto.MessageType;
import com.thoughtapps.droppoint.core.messageExchange.config.SshServerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.common.compression.BuiltinCompressions;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.forward.PortForwardingEventListener;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.forward.AcceptAllForwardingFilter;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

/**
 * Created by zaskanov on 02.04.2017.
 */

//One instance per Application
@Slf4j
public class SshServerImpl implements SshServer {
    private org.apache.sshd.server.SshServer embeddedServer;
    final private SshServerConfig config;
    final private Map<MessageType, MessageProcessor> messageProcessors;
    private boolean serverOpen = false;
    private SshServerListener listener;

    public SshServerImpl(SshServerConfig config, Map<MessageType, MessageProcessor> messageProcessors) {
        this.config = config;
        this.messageProcessors = messageProcessors;
    }

    @Override
    public void setListener(SshServerListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean isOpen() {
        return serverOpen && embeddedServer.isOpen();
    }

    @Override
    public void init() {
        log.debug("Start ssh server initialization");

        try {
            org.apache.sshd.server.SshServer sshServer = org.apache.sshd.server.SshServer.setUpDefaultServer();
            sshServer.setHost(config.getHost());
            sshServer.setPort(config.getPort());
            sshServer.setTcpipForwardingFilter(AcceptAllForwardingFilter.INSTANCE);
            sshServer.setPasswordAuthenticator(new PasswordAuthenticator() {
                @Override
                public boolean authenticate(String username, String password, ServerSession session) throws PasswordChangeRequiredException {
                    return username.equals(config.getUsername()) && password.equals(config.getPassword());
                }
            });
            sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());

            if (config.isSftpEnabled()) {
                sshServer.setFileSystemFactory(new VirtualFileSystemFactory(Paths.get(config.getSftpRootDir())));
                sshServer.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));
            }

            sshServer.setCommandFactory(new CommandFactory() {
                @Override
                public Command createCommand(String command) {
                    return new SshServerCommand(messageProcessors);
                }
            });

            sshServer.addSessionListener(new SessionListener() {
                @Override
                public void sessionClosed(Session session) {
                    if (listener != null) listener.sessionClosed(session);
                }
            });
            sshServer.addPortForwardingEventListener(new PortForwardingEventListener() {
                @Override
                public void establishedExplicitTunnel(Session session, SshdSocketAddress local, SshdSocketAddress remote, boolean localForwarding, SshdSocketAddress boundAddress, Throwable reason) throws IOException {
                    if (listener != null) listener.tunnelEstablished(session, local, boundAddress);
                }
            });
            sshServer.setCompressionFactoriesNames(BuiltinCompressions.Constants.NONE, BuiltinCompressions.Constants.ZLIB,
                    BuiltinCompressions.Constants.DELAYED_ZLIB);

            sshServer.start();
            embeddedServer = sshServer;
            serverOpen = true;
        } catch (IOException e) {
            log.error("Error while initializing ssh server", e);
            throw new RuntimeException("Failed to init ssh server", e);
        }
        log.debug("Ssh server initialized");
    }

    @Override
    public void close() {
        log.debug("Start ssh server destroying");
        try {
            if (!serverOpen) return;

            serverOpen = false;
            embeddedServer.close(true);
            embeddedServer = null;
            listener = null;
        } finally {
            log.debug("Start ssh server destroyed");
        }
    }
}
