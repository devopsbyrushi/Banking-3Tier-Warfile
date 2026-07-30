package com.rushi.securebank.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * ================================================================
 *  PasswordUtil - SecureBank Application
 *  Trainer: Rushi | DevOps Multi-Cloud Training
 * ================================================================
 *
 *  Replaces Spring Security's BCryptPasswordEncoder.
 *
 *  Uses PBKDF2-HMAC-SHA256, which ships inside the JDK, so the
 *  project needs ZERO external security dependencies.
 *
 *  Stored format:  base64(salt) + ":" + base64(derivedKey)
 * ================================================================
 */
public final class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    /** Hashes a raw password with a fresh random salt. */
    public static String hash(String rawPassword) {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        RANDOM.nextBytes(salt);
        byte[] derived = pbkdf2(rawPassword.toCharArray(), salt);
        return Base64.getEncoder().encodeToString(salt)
                + ":"
                + Base64.getEncoder().encodeToString(derived);
    }

    /** Constant-time check of a raw password against a stored hash. */
    public static boolean matches(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        String[] parts = storedHash.split(":");
        if (parts.length != 2) {
            return false;
        }
        try {
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expected = Base64.getDecoder().decode(parts[1]);
            byte[] actual = pbkdf2(rawPassword.toCharArray(), salt);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to hash password", e);
        } finally {
            spec.clearPassword();
        }
    }
}
