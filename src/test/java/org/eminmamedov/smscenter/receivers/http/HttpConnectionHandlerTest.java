package org.eminmamedov.smscenter.receivers.http;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eminmamedov.smscenter.common.DbValueUtils;
import org.eminmamedov.smscenter.common.SpringTestSupport;
import org.eminmamedov.smscenter.dao.SenderMapper;
import org.eminmamedov.smscenter.dao.SmsMapper;
import org.eminmamedov.smscenter.dao.UserMapper;
import org.eminmamedov.smscenter.datamodel.MessageStatus;
import org.eminmamedov.smscenter.datamodel.SMSMessage;
import org.eminmamedov.smscenter.datamodel.Sender;
import org.eminmamedov.smscenter.datamodel.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.thoughtworks.xstream.XStream;

public class HttpConnectionHandlerTest extends SpringTestSupport {

    private static final String CHARSET = "UTF-8";

    @Autowired
    private HttpConnectionHandler handler;
    @Autowired
    private SmsMapper smsMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SenderMapper senderMapper;
    private XStream xstream;
    private List<Message> messages;
    private User user;
    private Sender sender;

    @Before
    public void setUp() {
        xstream = new XStream();
        xstream.processAnnotations(HttpResponse.class);
        xstream.processAnnotations(HttpRequest.class);
    }

    @Test
    public void testHandleWrongXml() throws IOException {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRemoteAddress()).thenReturn(new InetSocketAddress(80));
        byte[] bytes = "WRONG".getBytes(CHARSET);
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(bytes));
        Headers headers = mock(Headers.class);
        when(exchange.getResponseHeaders()).thenReturn(headers);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(out);

        handler.handle(exchange);
        verify(exchange, times(1)).sendResponseHeaders(eq(200), eq(0L));
        String responseXml = new String(out.toByteArray(), CHARSET);
        HttpResponse response = (HttpResponse) xstream.fromXML(responseXml);
        assertNotNull(response);
        assertEquals(new Integer(HttpReceiverErrorCodes.ERR_INV_XML), response.getErrorCode());
    }

    @Test
    public void testHandleInvalidUser() throws IOException {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRemoteAddress()).thenReturn(new InetSocketAddress(80));
        String requestXml = createSendRequest();
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestXml.getBytes(CHARSET)));
        Headers headers = mock(Headers.class);
        when(exchange.getResponseHeaders()).thenReturn(headers);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(out);

        handler.handle(exchange);
        verify(exchange, times(1)).sendResponseHeaders(eq(200), eq(0L));
        String responseXml = new String(out.toByteArray(), CHARSET);
        HttpResponse response = (HttpResponse) xstream.fromXML(responseXml);
        assertNotNull(response);
        assertEquals(new Integer(HttpReceiverErrorCodes.ERR_INV_PASWD), response.getErrorCode());
    }

    @Test
    public void testHandleOk() throws IOException {
        user = new User();
        user.setName("testUser");
        user.setPassword("testPass");
        user.setEnabled(true);
        userMapper.insert(user);

        sender = new Sender();
        sender.setSign("testSender");
        senderMapper.insert(sender);

        senderMapper.createLink(user, sender);

        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRemoteAddress()).thenReturn(new InetSocketAddress(80));
        String requestXml = createSendRequest();
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestXml.getBytes(CHARSET)));
        Headers headers = mock(Headers.class);
        when(exchange.getResponseHeaders()).thenReturn(headers);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(out);

        handler.handle(exchange);
        verify(exchange, times(1)).sendResponseHeaders(eq(200), eq(0L));
        String responseXml = new String(out.toByteArray(), CHARSET);
        HttpResponse response = (HttpResponse) xstream.fromXML(responseXml);
        assertNotNull(response);
        assertNull(response.getErrorCode());
        messages = response.getSendMessages();
        assertNotNull(messages);
        assertFalse(messages.isEmpty());
        assertEquals(4, messages.size());
        assertEquals(new Integer(HttpReceiverErrorCodes.ERR_INV_SRC_ADR), messages.get(0).getErrorCode());
        assertNull(messages.get(1).getErrorCode());
        assertNull(messages.get(2).getErrorCode());
        assertNull(messages.get(3).getErrorCode());

        out.reset();
        requestXml = createCheckRequest(messages.get(1).getServerId());
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestXml.getBytes(CHARSET)));
        handler.handle(exchange);
        verify(exchange, times(2)).sendResponseHeaders(eq(200), eq(0L));
        responseXml = new String(out.toByteArray(), CHARSET);
        response = (HttpResponse) xstream.fromXML(responseXml);
        assertNotNull(response);
        assertNull(response.getErrorCode());
        List<Message> checkedMessages = response.getCheckMessages();
        assertNotNull(checkedMessages);
        assertEquals(1, checkedMessages.size());
        assertNull(checkedMessages.get(0).getErrorCode());
        assertEquals(DbValueUtils.getDbValue(MessageStatus.NEW_MESSAGE_STATE), checkedMessages.get(0).getStatus());
    }

    private String createSendRequest() {
        HttpRequest request = new HttpRequest();
        request.setLogin("testUser");
        request.setPassword("testPass");
        Message message1 = new Message();
        message1.setClientId(1);
        message1.setReceiver("79853869839");
        message1.setText("Test message");
        message1.setSender("wrongSender");

        Message message2 = new Message();
        message2.setClientId(2);
        message2.setReceiver("79853869839");
        message2.setText("Test message");
        message2.setSender("testSender");

        Message message3 = new Message();
        message3.setClientId(2);
        message3.setReceiver("79853869839");
        message3.setText(StringUtils.leftPad("", 100, 'ô'));

        request.setSendMessages(new ArrayList<Message>(Arrays.asList(message1, message2, message3)));

        return xstream.toXML(request);
    }

    private String createCheckRequest(Long id) {
        HttpRequest request = new HttpRequest();
        request.setLogin("testUser");
        request.setPassword("testPass");
        Message message1 = new Message();
        message1.setServerId(id);

        request.setCheckMessages(new ArrayList<Message>(Arrays.asList(message1)));

        return xstream.toXML(request);
    }

    @After
    public void tearDown() {
        if (messages != null) {
            for (Message message : messages) {
                SMSMessage smsMessage = new SMSMessage();
                smsMessage.setId(message.getServerId());
                smsMapper.delete(smsMessage);
            }
        }
        if (sender != null) {
            senderMapper.removeLinks(sender);
            senderMapper.delete(sender);
        }
        if (user != null) {
            userMapper.delete(user);
        }
    }

}
