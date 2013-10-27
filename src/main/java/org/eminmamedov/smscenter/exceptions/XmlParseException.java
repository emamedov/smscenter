package org.eminmamedov.smscenter.exceptions;

/**
 * Is thrown if any error during parsing XML file occured
 * 
 * @author Emin Mamedov
 * 
 */
public class XmlParseException extends SmsCenterException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new XmlParseException with the specified detail message and
     * cause.
     * <p>
     * Note that the detail message associated with <code>cause</code> is
     * <i>not</i> automatically incorporated in this exception's detail message.
     * 
     * @param message
     *            the detail message (which is saved for later retrieval by the
     *            {@link #getMessage()} method).
     * @param cause
     *            the cause (which is saved for later retrieval by the
     *            {@link #getCause()} method). (A <tt>null</tt> value is
     *            permitted, and indicates that the cause is nonexistent or
     *            unknown.)
     */
    public XmlParseException(String message, Throwable cause) {
        super(message, cause);
    }

}
