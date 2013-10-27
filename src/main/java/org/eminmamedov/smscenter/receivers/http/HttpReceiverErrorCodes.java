package org.eminmamedov.smscenter.receivers.http;

public interface HttpReceiverErrorCodes {

    /** Generic System Error */
    int SYS_ERR = 1;
    /** Wrong password */
    int ERR_INV_PASWD = 2;
    /** Wrong sender's address */
    int ERR_INV_SRC_ADR = 3;
    /** Wrong receiver's address */
    int ERR_INV_DST_ADR = 4;
    /** Wrong XML format */
    int ERR_INV_XML = 6;
    /** Wrong text length */
    int ERR_INV_TEXT_LENGTH = 7;

}
