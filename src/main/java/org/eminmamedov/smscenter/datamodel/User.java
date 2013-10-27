package org.eminmamedov.smscenter.datamodel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Domain class describes User business object
 *
 * @author Emin Mamedov
 *
 */
public class User extends Entity {

    private static final long serialVersionUID = 1L;

    private String name;
    private String password;
    private String encryptedPassword;
    private Date lastLogin;
    private boolean enabled;
    private List<Sender> senders = new ArrayList<Sender>();
    private String allowedIp;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<Sender> getSenders() {
        return senders;
    }

    public void setSenders(List<Sender> senders) {
        this.senders = senders;
    }

    public String getAllowedIp() {
        return allowedIp;
    }

    public void setAllowedIp(String allowedIp) {
        this.allowedIp = allowedIp;
    }

    public String getPassword() {
        return password;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public void setPassword(String password) {
        this.password = password;
        this.encryptedPassword = DigestUtils.sha512Hex(password);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("User [name=");
        builder.append(name);
        builder.append(", password=");
        builder.append(password);
        builder.append(", encryptedPassword=");
        builder.append(encryptedPassword);
        builder.append(", lastLogin=");
        builder.append(lastLogin);
        builder.append(", enabled=");
        builder.append(enabled);
        builder.append(", senders=");
        builder.append(senders);
        builder.append(", allowedIp=");
        builder.append(allowedIp);
        builder.append("]");
        return builder.toString();
    }

}
