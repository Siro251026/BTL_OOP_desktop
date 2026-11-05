package vn.nhom6.registration.service;

import vn.nhom6.registration.model.*;
import vn.nhom6.registration.store.InMemoryStore;

import java.util.*;

public final class AuthService {
    private final InMemoryStore store;
    private User currentUser;

    public AuthService(InMemoryStore store) {
        this.store = store;
    }

    public synchronized User login(String username, char[] password) {
        currentUser = null;
        User found =
                store.users().stream()
                        .filter(u -> u.getAccount().getUsername().equals(username.trim()))
                        .findFirst()
                        .orElse(null);
        if (found == null || !found.getAccount().matches(password))
            throw new SecurityException("Tài khoản hoặc mật khẩu không đúng.");
        return currentUser = found;
    }

    public Student register(String username, char[] password, String name, String email) {
        synchronized (store) {
            String key = Checks.text(username, "Tài khoản");
            if (store.users().stream()
                    .anyMatch(u -> u.getAccount().getUsername().equalsIgnoreCase(key)))
                throw new IllegalArgumentException("Tài khoản đã tồn tại.");
            Student s =
                    new Student(
                            "SV-" + UUID.randomUUID().toString().substring(0, 8),
                            name,
                            email,
                            new Account(key, password),
                            Set.of());
            store.add(s);
            return s;
        }
    }

    public synchronized void logout() {
        currentUser = null;
    }

    public synchronized User currentUser() {
        if (currentUser == null) throw new SecurityException("Bạn cần đăng nhập.");
        return currentUser;
    }

    public Student requireStudent() {
        User user = currentUser();
        if (!(user instanceof Student student))
            throw new SecurityException("Chức năng chỉ dành cho sinh viên.");
        return student;
    }

    public void requireAdmin() {
        if (currentUser().getRole() != UserRole.ADMIN)
            throw new SecurityException("Bạn không có quyền Admin.");
    }
}
