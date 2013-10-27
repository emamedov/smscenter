package org.eminmamedov.smscenter.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation is used to link database value (string or int) and Enum value.
 * 
 * @author Emin Mamedov
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DBValue {

    /**
     * Declares string value that should be associated with Enum value.
     */
    String stringValue() default "";

    /**
     * Declares int value that should be associated with Enum value. Is used
     * only if stringValue was not specified.
     */
    int intValue() default Integer.MIN_VALUE;

}
