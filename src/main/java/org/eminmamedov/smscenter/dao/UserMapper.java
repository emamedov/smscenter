package org.eminmamedov.smscenter.dao;

import org.eminmamedov.smscenter.datamodel.User;
import org.springframework.stereotype.Repository;

/**
 * Methods that should be implemented to work with users in database.
 * 
 * @author Emin Mamedov
 * 
 */
@Repository
public interface UserMapper {

    /**
     * Saves new user into database
     * 
     * @param user
     *            user that should be saved
     */
    void insert(User user);

    /**
     * Updates specified user
     * 
     * @param user
     *            user that should be updated
     */
    void update(User user);

    /**
     * Returns user by specified login and password
     * 
     * @param name
     *            user's name
     * @param password
     *            user's password
     * @return found user or null
     */
    User getUser(String name, String password);

    /**
     * Increase count of errors for specified user
     * 
     * @param userName
     *            name of user that has input wrong password
     */
    void wrongPassword(String userName);

    /**
     * Removes specified user from database
     * 
     * @param user
     *            user that should be deleted from database
     */
    void delete(User user);

}
