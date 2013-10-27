package org.eminmamedov.smscenter.datamodel;

import org.eminmamedov.smscenter.common.DBValue;
import org.eminmamedov.smscenter.common.DbValueUtils;

/**
 * Possible message statuses
 *
 * @author Emin Mamedov
 *
 */
public enum MessageStatus {

    @DBValue(intValue=-2)
    SERVER_HASNT_RECEIVED,
    @DBValue(intValue=-1)
    MESSAGE_WASNT_DELIVERED,
    @DBValue(intValue=0)
    NEW_MESSAGE_STATE,
    @DBValue(intValue=1)
    MESSAGE_IN_QUEUE,
    @DBValue(intValue=2)
    MESSAGE_WAS_DELIVERED,
    @DBValue(intValue=3)
    MESSAGE_IN_QUEUE_WASNT_SUBMIT,
    @DBValue(intValue=4)
    MESSAGE_RESERVED_BY_CHANNEL;

    public int getDbValue() {
        return (Integer) DbValueUtils.getDbValue(this);
    }

}
