package org.eminmamedov.smscenter.datamodel;

/**
 * Domain class describes Channel business object
 * 
 * @author Emin Mamedov
 * 
 */
public class Channel extends Entity {

    private static final long serialVersionUID = 1L;

    private String name;
    private String login;
    private String password;
    private String host;
    private int port;
    private boolean enabled;
    private int sendSpeed;
    private int checkSpeed;
    private String bindType;
    private int bindTON;
    private int bindNPI;
    private int sourceAddrTON;
    private int sourceAddrNPI;
    private int destAddrTON;
    private int destAddrNPI;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getSendSpeed() {
        return sendSpeed;
    }

    public void setSendSpeed(int sendSpeed) {
        this.sendSpeed = sendSpeed;
    }

    public int getCheckSpeed() {
        return checkSpeed;
    }

    public void setCheckSpeed(int checkSpeed) {
        this.checkSpeed = checkSpeed;
    }

    public String getBindType() {
        return bindType;
    }

    public void setBindType(String bindType) {
        this.bindType = bindType;
    }

    public int getBindTON() {
        return bindTON;
    }

    public void setBindTON(int bindTON) {
        this.bindTON = bindTON;
    }

    public int getBindNPI() {
        return bindNPI;
    }

    public void setBindNPI(int bindNPI) {
        this.bindNPI = bindNPI;
    }

    public int getSourceAddrTON() {
        return sourceAddrTON;
    }

    public void setSourceAddrTON(int sourceAddrTON) {
        this.sourceAddrTON = sourceAddrTON;
    }

    public int getSourceAddrNPI() {
        return sourceAddrNPI;
    }

    public void setSourceAddrNPI(int sourceAddrNPI) {
        this.sourceAddrNPI = sourceAddrNPI;
    }

    public int getDestAddrTON() {
        return destAddrTON;
    }

    public void setDestAddrTON(int destAddrTON) {
        this.destAddrTON = destAddrTON;
    }

    public int getDestAddrNPI() {
        return destAddrNPI;
    }

    public void setDestAddrNPI(int destAddrNPI) {
        this.destAddrNPI = destAddrNPI;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Channel [name=");
        builder.append(name);
        builder.append(", login=");
        builder.append(login);
        builder.append(", password=");
        builder.append(password);
        builder.append(", host=");
        builder.append(host);
        builder.append(", port=");
        builder.append(port);
        builder.append(", enabled=");
        builder.append(enabled);
        builder.append(", sendSpeed=");
        builder.append(sendSpeed);
        builder.append(", checkSpeed=");
        builder.append(checkSpeed);
        builder.append(", bindType=");
        builder.append(bindType);
        builder.append(", bindTON=");
        builder.append(bindTON);
        builder.append(", bindNPI=");
        builder.append(bindNPI);
        builder.append(", sourceAddrTON=");
        builder.append(sourceAddrTON);
        builder.append(", sourceAddrNPI=");
        builder.append(sourceAddrNPI);
        builder.append(", destAddrTON=");
        builder.append(destAddrTON);
        builder.append(", destAddrNPI=");
        builder.append(destAddrNPI);
        builder.append("]");
        return builder.toString();
    }

}
