package org.eminmamedov.smscenter.dao;

import org.eminmamedov.smscenter.datamodel.Sender;
import org.eminmamedov.smscenter.datamodel.User;
import org.springframework.stereotype.Repository;

/**
 * Methods that should be implemented to work with senders in database.
 * 
 * @author Emin Mamedov
 * 
 */
@Repository
public interface SenderMapper {

    /**
     * Saves sender into database
     * 
     * @param sender
     *            sender that should be saved in database
     */
    void insert(Sender sender);

    /**
     * Removes specified channel from database
     * 
     * @param sender
     *            sender that should be removed from database
     */
    void delete(Sender sender);

    /**
     * Find sender by sender sign
     * 
     * @param sign
     *            sign of sender that should be found
     * @return sender with specified sender sign or null if not found
     */
    Sender getSender(String sign);

    /**
     * Removes all links to specified sender
     * 
     * @param sender
     *            sender which links to user should be removed
     */
    void removeLinks(Sender sender);

    /**
     * Creates link between user and sender
     * 
     * @param user
     *            user that what to be linked to specified sender
     * @param sender
     *            sender that should be linked to specified user
     */
    void createLink(User user, Sender sender);

}
