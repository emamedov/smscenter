package org.eminmamedov.smscenter.receivers.http;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("response")
public class HttpResponse {

    private List<Message> sendMessages;
    private List<Message> checkMessages;
    private Integer errorCode;

    public List<Message> getSendMessages() {
        return sendMessages;
    }

    public void setSendMessages(List<Message> sendMessages) {
        this.sendMessages = sendMessages;
    }

    public List<Message> getCheckMessages() {
        return checkMessages;
    }

    public void setCheckMessages(List<Message> checkMessages) {
        this.checkMessages = checkMessages;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

}
