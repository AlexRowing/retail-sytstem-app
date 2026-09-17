package shelfside;

/**
 * Thrown by {@link Storage} when the inventory file cannot be read, written, or
 * understood. It carries a plain-English message so the app can print a helpful
 * line instead of crashing with a stack trace.
 */
public class StorageException extends Exception {

    private static final long serialVersionUID = 1L;

    public StorageException(String message) {
        super(message);
    }
}
