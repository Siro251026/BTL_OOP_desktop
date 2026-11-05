package vn.nhom6.registration.model;

import java.util.Objects;

public abstract class User {
    private final String id;
    private String fullName;
    private String email;
    private final Account account;

    protected User(String id, String name, String email, Account account) {
        this.id = Checks.text(id, "Mã người dùng");
        this.account = Objects.requireNonNull(account);
        updateProfile(name, email);
    }

    public final void updateProfile(String name, String email) {
        String checkedName = Checks.text(name, "Họ tên");
        String checkedEmail = Checks.text(email, "Email");
        fullName = checkedName;
        this.email = checkedEmail;
    }

    public abstract UserRole getRole();

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public Account getAccount() {
        return account;
    }

    @Override
    public String toString() {
        return id + " - " + fullName;
    }
}
