package org.eminmamedov.smscenter.dao;

import java.util.Date;
import java.util.List;

import org.eminmamedov.smscenter.datamodel.Channel;
import org.eminmamedov.smscenter.datamodel.MessageStatus;
import org.eminmamedov.smscenter.datamodel.SMSMessage;
import org.eminmamedov.smscenter.datamodel.User;
import org.springframework.stereotype.Repository;

/**
 * Methods that should be implemented to work with sms messages in database.
 * 
 * @author Emin Mamedov
 * 
 */
@Repository
public interface SmsMapper {

    /**
     * Returns max message id
     * 
     * @return max message id
     */
    Integer getLastSmsId();

    /**
     * Returns message by id
     * 
     * @param id
     *            id of message that should be found
     * @return found message
     */
    SMSMessage getMessage(Long id);

    /**
     * Returns messages by ids
     * 
     * @param ids
     *            ids of messages that should be found
     * @return list of found messages
     */
    List<SMSMessage> getMessages(List<Long> ids);

    /**
     * Returns messages by client ids
     * 
     * @param user
     * @param ids
     *            ids of messages that should be found
     * @return list of found messages
     */
    List<SMSMessage> getMessagesByClientIds(User user, List<Long> ids);

    /**
     * Saves incoming list of messages into database
     * 
     * @param messages
     *            list of messages that should be saved
     */
    void addMessages(List<SMSMessage> messages);

    /**
     * Updates SMS message
     * 
     * @param message
     *            message that should be updated
     */
    void update(SMSMessage message);

    /**
     * Removes specified message from database
     * 
     * @param message
     *            message that should be removed
     */
    void delete(SMSMessage message);

    /**
     * Returns list of messages with statuses
     * {@link SMSMessage.MESSAGE_WASNT_DELIVERED} and
     * {@link SMSMessage.MESSAGE_WAS_DELIVERED} of specified user that has
     * informed flag = 0. Size of list is limited by count parameter.
     * 
     * @param user
     *            user whose messages should be returned
     * @param count
     *            max count of messages
     * @return list of found messages
     */
    List<SMSMessage> getUpdatedMessages(User user, int count);

    /**
     * Set informed flag to 1 for message with specified id
     * 
     * @param id
     *            id of message
     * @param informed
     *            informed flag value
     * 
     */
    void setInformed(Long id, boolean informed);

    /**
     * Returns list of messages by state that should be send via specified
     * channel
     * 
     * @param channel
     * @param state
     * @param count
     * @param lastUpdated
     * @return list of messages that should be send via specified channel
     */
    List<SMSMessage> getMessagesByState(Channel channel, MessageStatus state, int count, Date lastUpdatedBefore);

    /**
     * Returns message by SMSC id
     * 
     * @param channel
     *            channel to the SMSC
     * @param smscId
     *            id of the message in SMSC
     * @return found message
     */
    SMSMessage getMessageBySmscId(Channel channel, String smscId);

}
