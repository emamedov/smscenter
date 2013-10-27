package org.eminmamedov.smscenter.datamodel;

/**
 * Domain class describes Sender business object
 * 
 * @author Emin Mamedov
 * 
 */
public class Sender extends Entity {

    private static final long serialVersionUID = 1L;

    private String sign;

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Sender [sign=");
        builder.append(sign);
        builder.append("]");
        return builder.toString();
    }

}
