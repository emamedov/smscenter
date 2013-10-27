package org.eminmamedov.smscenter.exceptions;

/**
 * Is thrown if user has not been found
 *
 * @author Emin Mamedov
 *
 */
public class NotFoundException extends SmsCenterException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(message);
    }

}
