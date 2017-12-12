package com.thoughtapps.droppoint.core.messageExchange.highLevel;

import com.thoughtapps.droppoint.core.dto.Message;
import com.thoughtapps.droppoint.core.dto.MessageType;
import com.thoughtapps.droppoint.core.helpers.CGSON;
import com.thoughtapps.droppoint.core.messageExchange.lowLevel.RequestReceiver;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.common.util.io.IoUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by zaskanov on 02.04.2017.
 */

/**
 * Used to process communication messages received by ssh server. Actual processing is in {@link #processors}
 */
@Slf4j
public class SshServerCommand implements Command, Runnable {

    public static Message UNKNOWN_COMMAND = new Message(MessageType.UNKNOWN_COMMAND, null);

    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback callback;

    private ExecutorService executors;
    private Future<?> pendingFuture;

    private Map<MessageType, MessageProcessor> processors;

    public SshServerCommand(Map<MessageType, MessageProcessor> processors) {
        this.processors = processors;
    }

    @Override
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
    }

    @Override
    public void start(Environment env) throws IOException {
        try {
            executors = Executors.newSingleThreadExecutor();
            pendingFuture = executors.submit(this);
        } catch (RuntimeException e) {    // e.g., RejectedExecutionException
            log.error("Exception during SshServerCommand startup", e);
            throw new IOException(e);
        }
    }

    @Override
    public void destroy() throws Exception {
        // if thread has not completed, cancel it
        if ((pendingFuture != null) && (!pendingFuture.isDone())) pendingFuture.cancel(true);

        pendingFuture = null;

        if (executors != null) executors.shutdownNow();

        executors = null;

        IoUtils.closeQuietly(in, out, err);
    }

    @Override
    public void run() {
        try {
            RequestReceiver receiver = new RequestReceiver(in, out, new RequestReceiver.ReceiverCallback() {

                @Override
                protected String onReceive(String request) {
                    Message requestMessage = CGSON.fromJson(request, Message.class);
                    MessageProcessor processor = processors.get(requestMessage.getType());

                    if (processor == null) return CGSON.toJson(UNKNOWN_COMMAND);
                    else return CGSON.toJson(processor.processMessage(requestMessage));
                }
            });
            receiver.listen();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
