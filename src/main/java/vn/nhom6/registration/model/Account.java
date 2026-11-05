package vn.nhom6.registration.model;

import java.security.*;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Stores only a salted hash. Demo credentials are documented in README. */
public final class Account {
    private final String username;
    private final byte[] salt = new byte[16];
    private final byte[] passwordHash;

    public Account(String username, char[] password) {
        this.username = Checks.text(username, "Tài khoản");
        if (password == null || password.length < 6)
            throw new IllegalArgumentException("Mật khẩu cần ít nhất 6 ký tự.");
        new SecureRandom().nextBytes(salt);
        passwordHash = hash(password);
    }

    private byte[] hash(char[] password) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, 120000, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Không khởi tạo được PBKDF2", e);
        } finally {
            spec.clearPassword();
        }
    }

    public boolean matches(char[] password) {
        if (password == null) return false;
        byte[] candidate = hash(password);
        try {
            return MessageDigest.isEqual(passwordHash, candidate);
        } finally {
            Arrays.fill(candidate, (byte) 0);
        }
    }

    public String getUsername() {
        return username;
    }
}
