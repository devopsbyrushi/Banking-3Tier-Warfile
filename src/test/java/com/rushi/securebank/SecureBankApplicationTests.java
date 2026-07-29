package com.rushi.securebank;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic smoke tests for SecureBank.
 * (For class purposes - real tests will be added in later modules)
 */
class SecureBankApplicationTests {

    @Test
    void simpleArithmeticTest() {
        int expected = 100;
        int actual = 50 + 50;
        assertEquals(expected, actual, "50 + 50 should equal 100");
    }

    @Test
    void brandNameTest() {
        String appName = "SecureBank";
        assertNotNull(appName);
        assertTrue(appName.contains("Bank"), "App name should contain 'Bank'");
    }
}
