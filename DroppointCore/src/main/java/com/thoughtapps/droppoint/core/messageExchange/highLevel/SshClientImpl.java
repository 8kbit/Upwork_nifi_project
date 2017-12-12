package com.thoughtapps.droppoint.core.messageExchange.highLevel;

import com.jcraft.jsch.*;
import com.thoughtapps.droppoint.core.dto.Message;
import com.thoughtapps.droppoint.core.helpers.CGSON;
import com.thoughtapps.droppoint.core.messageExchange.config.SshClientConfig;
import com.thoughtapps.droppoint.core.messageExchange.lowLevel.RequestSender;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by zaskanov on 02.04.2017.
 */
@Slf4j
public class SshClientImpl implements SshClient {

    private JSch embeddedClient;
    private Session clientSession;
    private final SshClientConfig config;
    private boolean clientOpen = false;
    private final Properties sessionConfig;

    public SshClientImpl(SshClientConfig config) {
        this.config = config;

        this.sessionConfig = new Properties();
        this.sessionConfig.put("StrictHostKeyChecking", "no");

        //compression support
        if (Boolean.TRUE.equals(config.getUseCompression())) {
            sessionConfig.put("compression.s2c", "zlib@openssh.com,zlib,none");
            sessionConfig.put("compression.c2s", "zlib@openssh.com,zlib,none");
            sessionConfig.put("compression_level", "9");
        } else {
            sessionConfig.setProperty("compression.s2c", "none");
            sessionConfig.setProperty("compression.c2s", "none");
        }
    }

    @Override
    public void init() {
        log.debug("Start ssh client initialization");

        embeddedClient = new JSch();
        clientOpen = true;
        if (!tryOpenSession()) throw new RuntimeException("Failed:- To init ssh client");

        log.debug("Ssh client initialized");
    }

    // try to connect to ssh server
    private boolean tryOpenSession() {
        try {
            if (!clientOpen) return false;
            if (clientSession != null && clientSession.isConnected()) return false;

            Session session = embeddedClient.getSession(config.getUsername(), config.getHost(), config.getPort());
            session.setPassword(config.getPassword());
            session.setConfig(sessionConfig);
            session.connect();
            clientSession = session;
            return true;
        } catch (JSchException e) {
        	if (log.isErrorEnabled())
        		log.error(e.getMessage());
        	else
        		log.info("Connection with server not established.");
            
        	return false;
        }
    }

    @Override
    public void close() {
        log.debug("Start ssh client destroying");
        try {
            if (clientSession != null) clientSession.disconnect();
            embeddedClient = null;
            clientOpen = false;
        } finally {
            log.debug("Ssh client destroyed");
        }
    }

    @Override
    public boolean isOpen() {
        return clientOpen && clientSession != null && clientSession.isConnected();
    }

    //send message to ssh server
    @Override
    public Message sendMessage(Message message) {
        tryOpenSession();
        checkOpen();

        ChannelExec channelExec = null;
        try {
            channelExec = (ChannelExec) clientSession.openChannel("exec");
            channelExec.connect();

            RequestSender sender = new RequestSender(channelExec.getInputStream(), channelExec.getOutputStream());

            return CGSON.fromJson(sender.sendRequest(CGSON.toJson(message)), Message.class);
        } catch (IOException | JSchException e) {
            throw new RuntimeException(e);
        } finally {
            if (channelExec != null && channelExec.isConnected()) channelExec.disconnect();
        }
    }

    //read file from ssh server acting as SFTP
    @Override
    public void readFile(String path, SshClient.InputStreamCallback callback) {
        tryOpenSession();
        checkOpen();

        ChannelSftp channelSftp = null;
        try {
            channelSftp = (ChannelSftp) clientSession.openChannel("sftp");
            channelSftp.connect();
            InputStream in = channelSftp.get(path);
            callback.read(in);
        } catch (JSchException | SftpException e) {
            throw new RuntimeException(e);
        } finally {
            if (channelSftp != null && channelSftp.isConnected()) channelSftp.disconnect();
        }
    }

    //remove file form ssh server acting as SFTP
    @Override
    public void removeFile(String path) {
        tryOpenSession();
        checkOpen();

        ChannelSftp channelSftp = null;
        try {
            channelSftp = (ChannelSftp) clientSession.openChannel("sftp");
            channelSftp.connect();
            channelSftp.rm(path);
        } catch (JSchException | SftpException e) {
            throw new RuntimeException(e);
        } finally {
            if (channelSftp != null && channelSftp.isConnected()) channelSftp.disconnect();
        }
    }

    //forward remote port (resides on ssh server side) to local port (reside on ssh client side)
    @Override
    public void forwardRemotePort(String remoteHost, int remotePort, String localHost, int localPort) {
        tryOpenSession();
        checkOpen();

        try {
            clientSession.setPortForwardingR(remoteHost, remotePort, localHost, localPort);
        } catch (JSchException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkOpen() {
        if (!isOpen()) throw new RuntimeException("Ssh client is closed");
    }
}
