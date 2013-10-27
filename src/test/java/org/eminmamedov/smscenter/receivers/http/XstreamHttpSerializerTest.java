package org.eminmamedov.smscenter.receivers.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.validation.Validator;

import org.apache.commons.io.IOUtils;
import org.eminmamedov.smscenter.exceptions.SmsCenterException;
import org.eminmamedov.smscenter.exceptions.XmlParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.xml.sax.SAXException;

@RunWith(MockitoJUnitRunner.class)
public class XstreamHttpSerializerTest {

    private XstreamHttpSerializer serializer;
    @Mock
    private Validator requestValidator;
    @Mock
    private Validator responseValidator;

    @Before
    public void setUp() throws Exception {
        this.serializer = new XstreamHttpSerializer();
        this.serializer.afterPropertiesSet();
    }

    @Test
    public void testDeserializeOk() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("request.xml");
        String xml = IOUtils.toString(is, "UTF-8");
        HttpRequest request = serializer.deserialize(xml);

        assertEquals("test", request.getLogin());
        assertEquals("test", request.getPassword());
        List<Message> sendMessages = request.getSendMessages();
        assertNotNull(sendMessages);
        assertFalse(sendMessages.isEmpty());
        Message sendMessage = sendMessages.get(0);
        assertEquals(new Integer(1), sendMessage.getClientId());
        assertEquals("79853869839", sendMessage.getReceiver());
        assertEquals("Test message", sendMessage.getText());
        List<Message> checkMessages = request.getCheckMessages();
        assertNotNull(checkMessages);
        assertFalse(checkMessages.isEmpty());
        Message checkMessage = checkMessages.get(0);
        assertEquals(new Long(1), checkMessage.getServerId());
    }

    @Test(expected = XmlParseException.class)
    public void testDeserializeSAXException() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("request.xml");
        byte[] data = new byte[is.available()];
        is.read(data);
        is.close();
        String xml = new String(data, "UTF-8");
        doThrow(new SAXException()).when(requestValidator).validate(any(Source.class));
        Whitebox.setInternalState(serializer, "requestValidator", requestValidator);
        serializer.deserialize(xml);
    }

    @Test(expected = XmlParseException.class)
    public void testDeserializeIOException() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("request.xml");
        byte[] data = new byte[is.available()];
        is.read(data);
        is.close();
        String xml = new String(data, "UTF-8");
        doThrow(new IOException()).when(requestValidator).validate(any(Source.class));
        Whitebox.setInternalState(serializer, "requestValidator", requestValidator);
        serializer.deserialize(xml);
    }

    @Test(expected = XmlParseException.class)
    public void testDeserializeRuntimeException() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("request.xml");
        byte[] data = new byte[is.available()];
        is.read(data);
        is.close();
        String xml = new String(data, "UTF-8");
        doThrow(new NullPointerException()).when(requestValidator).validate(any(Source.class));
        Whitebox.setInternalState(serializer, "requestValidator", requestValidator);
        serializer.deserialize(xml);
    }

    @Test(expected = SmsCenterException.class)
    public void testDeserializeException() throws Exception {
        String xml = "<wrongXml/>";
        HttpRequest request = serializer.deserialize(xml);

        assertEquals("test", request.getLogin());
        assertEquals("test", request.getPassword());
        List<Message> sendMessages = request.getSendMessages();
        assertNotNull(sendMessages);
        assertFalse(sendMessages.isEmpty());
        Message sendMessage = sendMessages.get(0);
        assertEquals(new Long(1), sendMessage.getClientId());
        assertEquals("79853869839", sendMessage.getReceiver());
        assertEquals("Test message", sendMessage.getText());
        List<Message> checkMessages = request.getCheckMessages();
        assertNotNull(checkMessages);
        assertFalse(checkMessages.isEmpty());
        Message checkMessage = checkMessages.get(0);
        assertEquals(new Long(1), checkMessage.getServerId());
    }

    @Test
    public void testSerializeOk() throws Exception {
        HttpResponse httpResponse = new HttpResponse();
        Message checkMessage = new Message();
        checkMessage.setServerId(1L);
        checkMessage.setStatus(1);
        checkMessage.setClientId(3);
        httpResponse.setCheckMessages(new ArrayList<Message>(Arrays.asList(checkMessage)));
        Message sendMessage = new Message();
        sendMessage.setClientId(1);
        sendMessage.setServerId(1L);
        sendMessage.setText("Test message");
        sendMessage.setGroupIndex(0);
        sendMessage.setGroupCount(0);
        httpResponse.setSendMessages(new ArrayList<Message>(Arrays.asList(sendMessage)));

        String responseXml = serializer.serialize(httpResponse);
        assertNotNull(responseXml);
        responseXml = responseXml.replaceAll(" ", "").replaceAll("\n", "").replaceAll("\r", "");

        InputStream is = this.getClass().getResourceAsStream("response.xml");
        byte[] data = new byte[is.available()];
        is.read(data);
        is.close();
        String expectedXml = new String(data, "UTF-8");
        expectedXml = expectedXml.replaceAll(" ", "").replaceAll("\n", "").replaceAll("\r", "");

        assertEquals(expectedXml, responseXml);
    }

    @Test(expected = XmlParseException.class)
    public void testSerializeSAXException() throws Exception {
        HttpResponse httpResponse = new HttpResponse();
        Message checkMessage = new Message();
        checkMessage.setServerId(1L);
        checkMessage.setStatus(1);
        httpResponse.setCheckMessages(new ArrayList<Message>(Arrays.asList(checkMessage)));
        Message sendMessage = new Message();
        sendMessage.setClientId(1);
        sendMessage.setServerId(1L);
        sendMessage.setText("Test message");
        sendMessage.setGroupIndex(0);
        sendMessage.setGroupCount(0);
        httpResponse.setSendMessages(new ArrayList<Message>(Arrays.asList(sendMessage)));

        doThrow(new SAXException()).when(responseValidator).validate(any(Source.class));
        Whitebox.setInternalState(serializer, "responseValidator", responseValidator);
        serializer.serialize(httpResponse);
    }

    @Test(expected = XmlParseException.class)
    public void testSerializeIOException() throws Exception {
        HttpResponse httpResponse = new HttpResponse();
        Message checkMessage = new Message();
        checkMessage.setServerId(1L);
        checkMessage.setStatus(1);
        httpResponse.setCheckMessages(new ArrayList<Message>(Arrays.asList(checkMessage)));
        Message sendMessage = new Message();
        sendMessage.setClientId(1);
        sendMessage.setServerId(1L);
        sendMessage.setText("Test message");
        sendMessage.setGroupIndex(0);
        sendMessage.setGroupCount(0);
        httpResponse.setSendMessages(new ArrayList<Message>(Arrays.asList(sendMessage)));

        doThrow(new IOException()).when(responseValidator).validate(any(Source.class));
        Whitebox.setInternalState(serializer, "responseValidator", responseValidator);
        serializer.serialize(httpResponse);
    }

    @Test(expected = XmlParseException.class)
    public void testSerializeRuntimeException() throws Exception {
        HttpResponse httpResponse = new HttpResponse();
        Message checkMessage = new Message();
        checkMessage.setServerId(1L);
        checkMessage.setStatus(1);
        httpResponse.setCheckMessages(new ArrayList<Message>(Arrays.asList(checkMessage)));
        Message sendMessage = new Message();
        sendMessage.setClientId(1);
        sendMessage.setServerId(1L);
        sendMessage.setText("Test message");
        sendMessage.setGroupIndex(0);
        sendMessage.setGroupCount(0);
        httpResponse.setSendMessages(new ArrayList<Message>(Arrays.asList(sendMessage)));

        doThrow(new NullPointerException()).when(responseValidator).validate(any(Source.class));
        Whitebox.setInternalState(serializer, "responseValidator", responseValidator);
        serializer.serialize(httpResponse);
    }

    @Test(expected = SmsCenterException.class)
    public void testSerializeException() throws Exception {
        HttpResponse httpResponse = new HttpResponse();
        Message checkMessage = new Message();
        checkMessage.setStatus(1);
        httpResponse.setCheckMessages(new ArrayList<Message>(Arrays.asList(checkMessage)));
        Message sendMessage = new Message();
        sendMessage.setClientId(1);
        sendMessage.setServerId(1L);
        sendMessage.setText("Test message");
        sendMessage.setGroupIndex(0);
        sendMessage.setGroupCount(0);
        httpResponse.setSendMessages(new ArrayList<Message>(Arrays.asList(sendMessage)));

        serializer.serialize(httpResponse);
    }

}
