package org.eminmamedov.smscenter.receivers.http;

import static org.eminmamedov.smscenter.receivers.http.HttpReceiverErrorCodes.ERR_INV_PASWD;
import static org.eminmamedov.smscenter.receivers.http.HttpReceiverErrorCodes.ERR_INV_XML;
import static org.eminmamedov.smscenter.receivers.http.HttpReceiverErrorCodes.SYS_ERR;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eminmamedov.smscenter.datamodel.User;
import org.eminmamedov.smscenter.exceptions.NotFoundException;
import org.eminmamedov.smscenter.exceptions.XmlParseException;
import org.eminmamedov.smscenter.services.SmsCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * <p>
 * Class handles request from HTTP Server, processes it and returns answer
 * </p>
 * 
 * @author Emin Mamedov
 * 
 */
@Component
public class HttpConnectionHandler implements HttpHandler {

    private static final Logger log = Logger.getLogger(HttpConnectionHandler.class);
    private static final String CHARSET = "UTF-8";
    private static final String WRONG_REQUEST = "<html><head><title>[400] Bad Request</title></head><body><h1>Bad Request</h1></body></html>";
    private static final int HTTP_OK = 200;
    private static final int HTTP_NOT_FOUND = 400;

    @Autowired
    private HttpSerializer xmlSerializer;
    @Autowired
    private SmsCenterService smsCenterService;
    @Autowired
    private HttpReceiverHelper httpReceiverHelper;

    @Override
    public void handle(HttpExchange exchange) {
        String ip = "Unknown";
        try {
            ip = exchange.getRemoteAddress().getAddress().getHostAddress();
            log.debug("Handle request from IP = " + ip);
            String requestXml = IOUtils.toString(exchange.getRequestBody(), CHARSET);
            log.debug("From IP = " + ip + " following data has been received:\n" + requestXml);
            HttpResponse response = processRequest(ip, requestXml);
            exchange.getResponseHeaders().put("Content-Type", Arrays.asList("text/plain; charset=" + CHARSET));
            if (response != null) {
                exchange.sendResponseHeaders(HTTP_OK, 0);
                String responseXml = xmlSerializer.serialize(response);
                sendResponse(exchange.getResponseBody(), responseXml);
            } else {
                exchange.sendResponseHeaders(HTTP_NOT_FOUND, 0);
                sendResponse(exchange.getResponseBody(), WRONG_REQUEST);
            }
        } catch (Exception e) {
            log.error("Error occurs during request processing", e);
        } finally {
            exchange.close();
            log.debug("Closing exchange for IP = " + ip);
        }
    }

    private void sendResponse(OutputStream out, String resp) throws IOException {
        log.info("Send response to client:\n" + resp);
        Writer writer = new OutputStreamWriter(out, CHARSET);
        writer.write(resp);
        writer.close();
    }

    private HttpResponse processRequest(String ip, String requestXml) throws IOException {
        if (requestXml == null) {
            return null;
        }
        HttpResponse response = new HttpResponse();
        String login = null;
        try {
            HttpRequest request = xmlSerializer.deserialize(requestXml);
            login = request.getLogin();
            String password = request.getPassword();
            log.info("Connecting via HTTP protocol. USER = " + login + " IP = " + ip);
            User foundedUser = smsCenterService.getUser(login, password, ip);
            log.debug("User with specified login has been found. USER = " + foundedUser);
            if (foundedUser.isEnabled()) {
                log.info("User successfully connected via HTTP. USER = " + foundedUser.getName());
                if (!CollectionUtils.isEmpty(request.getSendMessages())) {
                    response.setSendMessages(httpReceiverHelper.sendMessages(foundedUser, request.getSendMessages()));
                }
                if (!CollectionUtils.isEmpty(request.getCheckMessages())) {
                    response.setCheckMessages(httpReceiverHelper.checkMessages(foundedUser, request.getCheckMessages()));
                }
            } else {
                log.info("User is locked. USER = " + foundedUser);
                response.setErrorCode(ERR_INV_PASWD);
            }
        } catch (XmlParseException e) {
            log.error("Exception occurs during XML parsing", e);
            response.setErrorCode(ERR_INV_XML);
        } catch (NotFoundException e) {
            log.info("Wrong login and/or password. USER = " + login);
            response.setErrorCode(ERR_INV_PASWD);
        } catch (RuntimeException e) {
            log.error("Unknown error", e);
            response.setErrorCode(SYS_ERR);
        }
        return response;
    }

}
