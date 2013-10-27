package org.eminmamedov.smscenter.datamodel;

import java.util.Date;

/**
 * Domain class describe SMSMessage business object
 * 
 * @author Emin Mamedov
 * 
 */
public class SMSMessage extends Entity {

    private static final long serialVersionUID = 1L;

    private User user;
    private Sender sender;
    private Channel channel;
    private String senderSign;
    private String phone;
    private String text;
    private Date sendDate;
    private int smsGroupId;
    private int smsGroupCount;
    private int smsGroupIndex;
    private MessageStatus status;
    private int clientId = 0;
    private String smscId;
    private int version;

    public SMSMessage() {

    }

    public SMSMessage(Long id, int clientId, User user, Sender sender, String senderSign, String phone, String text,
            int smsGroupId, int smsGroupCount, int smsGroupIndex) {
        this.setId(id);
        this.user = user;
        this.sender = sender;
        this.senderSign = senderSign;
        this.phone = phone;
        this.text = text;
        this.smsGroupId = smsGroupId;
        this.smsGroupCount = smsGroupCount;
        this.smsGroupIndex = smsGroupIndex;
        this.status = MessageStatus.NEW_MESSAGE_STATE;
        this.clientId = clientId;
        this.sendDate = new Date();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public String getSenderSign() {
        return senderSign;
    }

    public void setSenderSign(String senderSign) {
        this.senderSign = senderSign;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public int getSmsGroupId() {
        return smsGroupId;
    }

    public void setSmsGroupId(int smsGroupId) {
        this.smsGroupId = smsGroupId;
    }

    public int getSmsGroupCount() {
        return smsGroupCount;
    }

    public void setSmsGroupCount(int smsGroupCount) {
        this.smsGroupCount = smsGroupCount;
    }

    public int getSmsGroupIndex() {
        return smsGroupIndex;
    }

    public void setSmsGroupIndex(int smsGroupIndex) {
        this.smsGroupIndex = smsGroupIndex;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getSmscId() {
        return smscId;
    }

    public void setSmscId(String smscId) {
        this.smscId = smscId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SMSMessage [user=");
        builder.append(user);
        builder.append(", sender=");
        builder.append(sender);
        builder.append(", channel=");
        builder.append(channel);
        builder.append(", senderSign=");
        builder.append(senderSign);
        builder.append(", phone=");
        builder.append(phone);
        builder.append(", text=");
        builder.append(text);
        builder.append(", sendDate=");
        builder.append(sendDate);
        builder.append(", smsGroupId=");
        builder.append(smsGroupId);
        builder.append(", smsGroupCount=");
        builder.append(smsGroupCount);
        builder.append(", smsGroupIndex=");
        builder.append(smsGroupIndex);
        builder.append(", status=");
        builder.append(status);
        builder.append(", clientId=");
        builder.append(clientId);
        builder.append(", smscId=");
        builder.append(smscId);
        builder.append(", version=");
        builder.append(version);
        builder.append("]");
        return builder.toString();
    }

}
