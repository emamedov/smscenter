package org.eminmamedov.smscenter.receivers.http;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

@RunWith(MockitoJUnitRunner.class)
public class HttpReceiverTest {

    private HttpReceiver receiver;
    @Mock
    private HttpConnectionHandler connectionHandler;

    @Before
    public void setUp() {
        receiver = new HttpReceiver();
        receiver.setPort(80);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                HttpExchange exchange = (HttpExchange) invocation.getArguments()[0];
                Headers headers = exchange.getResponseHeaders();
                headers.set("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, 0);
                Writer writer = new OutputStreamWriter(exchange.getResponseBody(), "UTF-8");
                writer.write("<html><head><title>[400] Bad Request</title></head><body><h1>Bad Request</h1></body></html>");
                writer.close();
                exchange.close();
                return null;
            }
        }).when(connectionHandler).handle(any(HttpExchange.class));
        receiver.setHttpConnectionHandler(connectionHandler);
    }

    @Test
    public void testStopWithoutStart() throws MalformedURLException {
        receiver.stop();
        URL url = new URL("http", "localhost", 80, "");
        try {
            url.getContent();
            fail();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testStopAfterStart() throws MalformedURLException {
        receiver.start();
        URL url = new URL("http", "localhost", 80, "");
        try {
            url.getContent();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        receiver.stop();
    }

    @Test
    public void testStart() throws MalformedURLException {
        receiver.start();
        URL url = new URL("http", "localhost", 80, "");
        try {
            url.getContent();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        verify(connectionHandler, times(1)).handle(any(HttpExchange.class));
        receiver.stop();
    }

}
