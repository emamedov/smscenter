package org.eminmamedov.smscenter.dao;

import java.util.List;

import org.eminmamedov.smscenter.datamodel.Channel;
import org.eminmamedov.smscenter.datamodel.Sender;
import org.springframework.stereotype.Repository;

/**
 * Methods that should be implemented to work with channels in database.
 * 
 * @author Emin Mamedov
 * 
 */
@Repository
public interface ChannelMapper {

    /**
     * Saves incoming channel into database
     * 
     * @param channel
     *            channel that should be saved
     */
    void insert(Channel channel);

    /**
     * Removed specified channel from database
     * 
     * @param channel
     *            channel that should be removed
     */
    void delete(Channel channel);

    /**
     * Returns list of all existing channels
     * 
     * @return list of all existing channels
     */
    List<Channel> findAll();

    /**
     * Creates link between sender and channel
     *
     * @param channel
     * @param sender
     */
    void createLink(Channel channel, Sender sender);

    /**
     * Removes all assigned to the channel links
     *
     * @param channel
     */
    void removeLinks(Channel channel);

}
