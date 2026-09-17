package shelfside;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Tests for the demo login check. */
public class LoginProcessorTest {

    private final LoginProcessor login = new LoginProcessor();

    @Test
    public void check_acceptsCorrectDemoCredentials() {
        assertTrue(login.check(LoginProcessor.DEMO_USERNAME, LoginProcessor.DEMO_PASSWORD));
    }

    @Test
    public void check_rejectsWrongPassword() {
        assertFalse(login.check(LoginProcessor.DEMO_USERNAME, "wrong"));
    }

    @Test
    public void check_rejectsWrongUsername() {
        assertFalse(login.check("someoneElse", LoginProcessor.DEMO_PASSWORD));
    }

    @Test
    public void check_rejectsNulls() {
        assertFalse(login.check(null, null));
    }
}
