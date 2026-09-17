package shelfside;

/**
 * Checks the retailer login.
 *
 * IMPORTANT: these are DEMO credentials for a classroom project only. The
 * username and password are written in plain text right here in the source, so
 * this is NOT real security and must never be used to protect anything real.
 */
public class LoginProcessor {

    /** Demo username (classroom use only). */
    public static final String DEMO_USERNAME = "manager";

    /** Demo password (classroom use only). */
    public static final String DEMO_PASSWORD = "shelfside";

    /** How many wrong tries the menu allows before giving up. */
    public static final int MAX_ATTEMPTS = 3;

    /** Returns true only if the username and password match the demo values. */
    public boolean check(String username, String password) {
        return DEMO_USERNAME.equals(username) && DEMO_PASSWORD.equals(password);
    }
}
