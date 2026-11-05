package vn.nhom6.registration.model;

public final class Admin extends User {
    public Admin(String id, String name, String email, Account account) {
        super(id, name, email, account);
    }

    @Override
    public UserRole getRole() {
        return UserRole.ADMIN;
    }
}
