package vn.nhom6.registration.model;

import java.time.LocalDateTime;
import java.util.Objects;

public final class Registration {
    private final String id;
    private final Student student;
    private final CourseSection section;
    private final LocalDateTime registeredAt;
    private RegistrationStatus status = RegistrationStatus.ACTIVE;
    private LocalDateTime cancelledAt;

    public Registration(String id, Student student, CourseSection section, LocalDateTime at) {
        this.id = Checks.text(id, "Mã đăng ký");
        this.student = Objects.requireNonNull(student);
        this.section = Objects.requireNonNull(section);
        registeredAt = Objects.requireNonNull(at);
    }

    public void cancel(LocalDateTime at) {
        if (!isActive()) throw new IllegalStateException("Đăng ký đã hủy.");
        cancelledAt = Objects.requireNonNull(at);
        status = RegistrationStatus.CANCELLED;
    }

    public boolean isActive() {
        return status == RegistrationStatus.ACTIVE;
    }

    public String getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public CourseSection getSection() {
        return section;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }
}
