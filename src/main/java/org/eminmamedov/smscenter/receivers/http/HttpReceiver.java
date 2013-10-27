package org.eminmamedov.smscenter.receivers.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.eminmamedov.smscenter.receivers.SmsReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.sun.net.httpserver.HttpServer;

/**
 * <p>
 * This class creates HTTP server to receive commands from client by HTTP
 * protocol.
 * </p>
 * 
 * <p>
 * Class calls HttpConnectionHandler when request from client is received
 * </p>
 * 
 * @author Emin Mamedov
 * 
 */
public class HttpReceiver implements SmsReceiver {

    private static final Logger log = Logger.getLogger(HttpReceiver.class);

    private HttpServer httpServer;
    @Value("${smscenter.receiver.http.port}")
    private Integer port;
    @Autowired
    private HttpConnectionHandler httpConnectionHandler;

    /**
     * Method starts HTTP Server
     * 
     * @see org.eminmamedov.smscenter.receivers.SmsReceiver#start()
     */
    @Override
    public void start() {
        log.debug("Starting server for HTTP protocol...");
        try {
            this.httpServer = HttpServer.create(new InetSocketAddress(this.port), 0);
            this.httpServer.createContext("/", this.httpConnectionHandler);
            this.httpServer.setExecutor(Executors.newCachedThreadPool());
            this.httpServer.start();
            log.info("Server has been started");
        } catch (IOException e) {
            log.error("Internal error occured during server start", e);
        } catch (Exception e) {
            log.error("FATAL UNKNOWN ERROR! SERVER HAS NOT BEEN STARTED!", e);
        }
    }

    /**
     * Method stops HTTP Server
     * 
     * @see org.eminmamedov.smscenter.receivers.SmsReceiver#stop()
     */
    @Override
    public void stop() {
        log.info("Stopping HTTP server");
        if (this.httpServer != null) {
            this.httpServer.stop(0);
        }
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setHttpConnectionHandler(HttpConnectionHandler httpConnectionHandler) {
        this.httpConnectionHandler = httpConnectionHandler;
    }

}