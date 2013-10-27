package org.eminmamedov.smscenter.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.eminmamedov.smscenter.exceptions.NotFoundException;
import org.eminmamedov.smscenter.exceptions.SmsCenterException;
import org.junit.Test;

public class ExceptionHandlerTest {

    private ExceptionHandler handler = new ExceptionHandler();

    @Test(expected = NotFoundException.class)
    public void testHandleExceptionSmsCenterException() {
        handler.handleException(new NotFoundException("TEST"));
    }

    @Test
    public void testHandleExceptionOtherException() {
        try {
            handler.handleException(new NullPointerException("TEST"));
            fail();
        } catch (SmsCenterException e) {
            assertEquals("Internal server error", e.getMessage());
            assertEquals(NullPointerException.class, e.getCause().getClass());
            assertEquals("TEST", e.getCause().getMessage());
        }
    }

}
