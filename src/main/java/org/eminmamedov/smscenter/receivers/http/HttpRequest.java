package org.eminmamedov.smscenter.receivers.http;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("request")
public class HttpRequest {

    private String login;
    private String password;
    private List<Message> sendMessages;
    private List<Message> checkMessages;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

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

}
