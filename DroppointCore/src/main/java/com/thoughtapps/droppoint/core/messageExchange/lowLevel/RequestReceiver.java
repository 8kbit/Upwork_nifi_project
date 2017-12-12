package com.thoughtapps.droppoint.core.messageExchange.lowLevel;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.NotImplementedException;

import java.io.*;

/**
 * Created by zaskanov on 02.04.2017.
 */

/**
 * Read message JSON from input stream and send response to output stream
 */
@Slf4j
public class RequestReceiver {
    private final BufferedReader reader;
    private final PrintStream writer;
    private final ReceiverCallback callback;

    public RequestReceiver(InputStream in, OutputStream out, ReceiverCallback callback) {
        reader = new BufferedReader(new InputStreamReader(in));
        writer = new PrintStream(out);
        this.callback = callback;
    }

    public void listen() throws IOException {
        log.debug("Start listening for request");

        String request = reader.readLine();

        log.debug("Request received");

        String response = callback.onReceive(request);
        writer.println(response);
        writer.flush();

        log.debug("Response sent");
    }

    public static class ReceiverCallback {
        protected String onReceive(String request) {
            throw new NotImplementedException();
        }
    }
}
