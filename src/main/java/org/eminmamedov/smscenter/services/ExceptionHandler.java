package org.eminmamedov.smscenter.services;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.eminmamedov.smscenter.exceptions.SmsCenterException;

@Aspect
public class ExceptionHandler {

    @AfterThrowing(pointcut = "within(org.eminmamedov.smscenter.services..*)", throwing = "error")
    public void handleException(Throwable error) {
        if (error instanceof SmsCenterException) {
            throw (SmsCenterException) error;
        }
        throw new SmsCenterException("Internal server error", error);
    }

}
