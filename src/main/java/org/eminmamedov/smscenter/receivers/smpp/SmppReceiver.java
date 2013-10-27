package org.eminmamedov.smscenter.receivers.smpp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.log4j.Logger;
import org.eminmamedov.smscenter.common.WorkerInitializer;
import org.eminmamedov.smscenter.receivers.SmsReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;

public class SmppReceiver implements SmsReceiver, Runnable {

    private static final Logger log = Logger.getLogger(SmppReceiver.class);

    @Autowired
    private SmppConnectionPool smppConnectionPool;
    @Value("${smscenter.receiver.smpp.port}")
    private int serverPort;
    @Value("${smscenter.receiver.smpp.timeout}")
    private int timeout;
    @Autowired
    @Qualifier("smsCenterTasksExecutor")
    private TaskExecutor tasksExecutor;
    @Autowired
    private WorkerInitializer workerInitializer;
    private volatile boolean started;

    @Override
    public void start() {
        if (!started) {
            started = true;
            tasksExecutor.execute(this);
        }
    }

    @Override
    public void stop() {
        if (started) {
            started = false;
        }
    }

    @Override
    public void run() {
        log.debug("Starting server for SMPP protocol...");
        ServerSocket smppServerSocket = null;
        try {
            smppServerSocket = new ServerSocket(serverPort);
            smppServerSocket.setSoTimeout(timeout);
            while (started) {
                try {
                    log.debug("Wait till client connect");
                    Socket clientSocket = smppServerSocket.accept();
                    log.info("Client has been connected with IP " + clientSocket.getInetAddress().getHostAddress());
                    SmppConnectionHandler handler = new SmppConnectionHandler(clientSocket);
                    workerInitializer.autowire(handler);
                    smppConnectionPool.add(handler);
                    tasksExecutor.execute(handler);
                } catch (SocketTimeoutException e) {
                    log.debug("Timeout has been reached");
                } catch (IOException e) {
                    log.error("Internal error occured during connection with client", e);
                }
            }
            log.debug("Stopping server for SMPP protocol...");
            smppConnectionPool.closeAll();
        } catch (IOException e) {
            log.error("Internal error occured during server start", e);
        } catch (RuntimeException e) {
            log.error("FATAL UNKNOWN ERROR! SERVER HAS NOT BEEN STARTED!", e);
        } finally {
            if (smppServerSocket != null) {
                try {
                    smppServerSocket.close();
                } catch (IOException e) {
                    log.warn("Internal error occured during server stop", e);
                }
            }
        }
    }

}
