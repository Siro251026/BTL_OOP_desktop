package vn.nhom6.registration.service;

import vn.nhom6.registration.exception.RegistrationException;
import vn.nhom6.registration.model.*;
import vn.nhom6.registration.observer.*;
import vn.nhom6.registration.store.InMemoryStore;

import java.time.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public final class RegistrationService {
    private final InMemoryStore store;
    private final AuthService auth;
    private final Clock clock;
    private final List<RegistrationObserver> observers = new CopyOnWriteArrayList<>();

    public RegistrationService(InMemoryStore store, AuthService auth, Clock clock) {
        this.store = store;
        this.auth = auth;
        this.clock = clock;
    }

    public void addObserver(RegistrationObserver observer) {
        observers.add(Objects.requireNonNull(observer));
    }

    public void removeObserver(RegistrationObserver observer) {
        observers.remove(observer);
    }

    private void publish(Registration r, RegistrationEvent event) {
        for (RegistrationObserver observer : observers) {
            // A display failure must not turn an already committed registration into a reported
            // failure.
            try {
                observer.onChanged(r, event);
            } catch (RuntimeException ex) {
                System.err.println("Observer: " + ex.getMessage());
            }
        }
    }

    public Registration register(CourseSection section) {
        Student student = auth.requireStudent();
        synchronized (store) {
            validateRegistration(student, section);
            Registration r =
                    new Registration(
                            UUID.randomUUID().toString().substring(0, 8),
                            student,
                            section,
                            LocalDateTime.now(clock));
            store.add(r);
            publish(r, RegistrationEvent.REGISTERED);
            return r;
        }
    }

    private void validateRegistration(Student student, CourseSection section) {
        if (!store.sections().contains(section))
            throw new RegistrationException("Lớp không tồn tại.");
        List<Registration> active =
                store.registrations().stream().filter(Registration::isActive).toList();
        if (active.stream().anyMatch(r -> r.getStudent() == student && r.getSection() == section))
            throw new RegistrationException("Bạn đã đăng ký lớp này.");
        if (availableSeats(section) <= 0) throw new RegistrationException("Lớp đã đầy.");
        if (section.getCourse().getPrerequisites().stream()
                .anyMatch(p -> !p.isSatisfiedBy(student)))
            throw new RegistrationException("Bạn chưa hoàn thành học phần tiên quyết.");
        if (active.stream()
                .anyMatch(r -> r.getStudent() == student && r.getSection().conflictsWith(section)))
            throw new RegistrationException("Trùng lịch với lớp đã đăng ký trong cùng học kỳ.");
    }

    public void drop(Registration registration) {
        User actor = auth.currentUser();
        synchronized (store) {
            if (!store.registrations().contains(registration))
                throw new RegistrationException("Không tìm thấy đăng ký.");
            if (actor.getRole() != UserRole.ADMIN && registration.getStudent() != actor)
                throw new SecurityException("Không được hủy đăng ký của sinh viên khác.");
            if (!registration.isActive())
                throw new RegistrationException("Đăng ký đã được hủy trước đó.");
            if (!registration.getSection().getSemester().canDrop(LocalDate.now(clock)))
                throw new RegistrationException("Đã quá thời hạn hủy.");
            registration.cancel(LocalDateTime.now(clock));
            publish(registration, RegistrationEvent.CANCELLED);
        }
    }

    public int activeCount(CourseSection section) {
        synchronized (store) {
            return (int)
                    store.registrations().stream()
                            .filter(r -> r.isActive() && r.getSection() == section)
                            .count();
        }
    }

    public int availableSeats(CourseSection section) {
        synchronized (store) {
            return section.getCapacity() - activeCount(section);
        }
    }

    public List<Registration> visibleRegistrations() {
        User user = auth.currentUser();
        return store.registrations().stream()
                .filter(r -> user.getRole() == UserRole.ADMIN || r.getStudent() == user)
                .toList();
    }
}
