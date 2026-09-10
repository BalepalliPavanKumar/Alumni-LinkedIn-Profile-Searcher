package com.alumni.linkedinsearcher.exception;

/**
 * Raised whenever the PhantomBuster integration fails: the launch call
 * errors out, the agent run times out, or its output cannot be parsed.
 */
public class PhantomBusterException extends RuntimeException {

    public PhantomBusterException(String message) {
        super(message);
    }

    public PhantomBusterException(String message, Throwable cause) {
        super(message, cause);
    }
}
