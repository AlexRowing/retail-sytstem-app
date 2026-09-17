package shelfside.exceptions;

/** Thrown when a search query finds no matching items, instead of crashing the program. */
public class NoResultsException extends Exception {
    public NoResultsException(String message) {
        super(message);
    }
}
