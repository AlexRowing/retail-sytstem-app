package shelfside.exceptions;

/** Thrown when a customer attempts to check out with nothing in their cart. */
public class EmptyCartException extends Exception {
    public EmptyCartException(String message) {
        super(message);
    }
}
