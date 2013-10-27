package org.eminmamedov.smscenter.receivers.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.eminmamedov.smscenter.exceptions.XmlParseException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;

@Component
public class XstreamHttpSerializer implements HttpSerializer, InitializingBean {

    private static final Logger log = Logger.getLogger(XstreamHttpSerializer.class);
    private static final URL REQUEST_XSD = XstreamHttpSerializer.class.getResource("request.xsd");
    private static final URL RESPONSE_XSD = XstreamHttpSerializer.class.getResource("response.xsd");

    private XStream xstream;
    private Validator requestValidator;
    private Validator responseValidator;

    @Override
    public void afterPropertiesSet() throws SAXException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema requestSchema = schemaFactory.newSchema(REQUEST_XSD);
        this.requestValidator = requestSchema.newValidator();
        Schema responseSchema = schemaFactory.newSchema(RESPONSE_XSD);
        this.responseValidator = responseSchema.newValidator();
        this.xstream = new XStream();
        this.xstream.processAnnotations(HttpRequest.class);
        this.xstream.processAnnotations(HttpResponse.class);
        this.xstream.processAnnotations(Message.class);
    }

    @Override
    public HttpRequest deserialize(String requestXml) {
        Exception ex = null;
        try {
            validateRequestXml(requestXml);
            return (HttpRequest) xstream.fromXML(requestXml);
        } catch (IOException e) {
            ex = e;
        } catch (SAXException e) {
            ex = e;
        } catch (RuntimeException e) {
            ex = e;
        }
        log.warn("Exception occurs while serializing response", ex);
        throw new XmlParseException(ex.getMessage(), ex);
    }

    @Override
    public String serialize(HttpResponse response) {
        Exception ex = null;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(outputStream, Charset.forName("UTF-8"));
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            xstream.toXML(response, writer);
            String responseXml = outputStream.toString("UTF-8");
            validateResponseXml(responseXml);
            return responseXml;
        } catch (IOException e) {
            ex = e;
        } catch (SAXException e) {
            ex = e;
        } catch (RuntimeException e) {
            ex = e;
        }
        log.warn("Exception occurs while serializing response", ex);
        throw new XmlParseException(ex.getMessage(), ex);
    }

    private void validateResponseXml(String responseXml) throws SAXException, IOException {
        Source source = new StreamSource(new StringReader(responseXml));
        responseValidator.validate(source);
    }

    private void validateRequestXml(String requestXml) throws SAXException, IOException {
        Source source = new StreamSource(new StringReader(requestXml));
        requestValidator.validate(source);
    }

}
