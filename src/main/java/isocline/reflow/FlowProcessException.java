package isocline.reflow;

/**
 * Exception thrown when process flow task occurs error.
 * This exception can be inspected using the Throwable.getCause() method.
 *
 */
public class FlowProcessException extends RuntimeException {


    /**
     * Constructs an FlowProcessException with no detail message.
     */
    public FlowProcessException() {
    }

    /**
     * Constructs an FlowProcessException with the specified detail message.
     * @param message the detail message
     */
    public FlowProcessException(String message) {
        super(message);
    }

    /**
     * Constructs an FlowProcessException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause (which is saved for later retrieval by the Throwable.getCause() method)
     */
    public FlowProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an FlowProcessException with the specified cause.
     * The detail message is set to (cause == null ? null : cause.toString())
     * (which typically contains the class and detail message of cause).

     * @param cause  the cause (which is saved for later retrieval by the Throwable.getCause() method)
     */
    public FlowProcessException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new runtime exception with the specified cause and a detail message
     * of (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause).
     * This constructor is useful for runtime exceptions that are little more than wrappers for other throwables.

     * @param message message - the detail message
     * @param cause the cause. (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     * @param enableSuppression whether or not suppression is enabled or disabled
     * @param writableStackTrace  whether or not the stack trace should be writable
     */
    public FlowProcessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
