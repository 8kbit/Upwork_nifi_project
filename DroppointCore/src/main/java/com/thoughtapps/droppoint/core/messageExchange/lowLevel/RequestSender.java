package com.thoughtapps.droppoint.core.messageExchange.lowLevel;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * Created by zaskanov on 02.04.2017.
 */

/**
 * send message JSON request to output stream and read response from input stream
 */
@Slf4j
public class RequestSender {

    private final BufferedReader reader;
    private final PrintStream writer;

    public RequestSender(InputStream in, OutputStream out) {
        reader = new BufferedReader(new InputStreamReader(in));
        writer = new PrintStream(out);
    }

    public String sendRequest(String message) throws IOException {
        log.debug("Start sending request");

        writer.println(message);
        writer.flush();

        log.debug("Request sent");

        String line = reader.readLine();
        log.debug("Response received");

        return line;
    }
}
