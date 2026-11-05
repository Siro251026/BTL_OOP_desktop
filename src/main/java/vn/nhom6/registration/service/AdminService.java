package vn.nhom6.registration.service;

import vn.nhom6.registration.model.*;
import vn.nhom6.registration.store.InMemoryStore;

import java.util.*;

public final class AdminService {
    private final InMemoryStore store;
    private final AuthService auth;
    private final RegistrationService registrations;

    public AdminService(InMemoryStore store, AuthService auth, RegistrationService registrations) {
        this.store = store;
        this.auth = auth;
        this.registrations = registrations;
    }

    private void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }

    private void validatePrerequisites(String id, List<Prerequisite> prerequisites) {
        for (Prerequisite p : prerequisites) {
            require(!p.getRequiredCourseId().equals(id), "Môn không được tiên quyết chính nó.");
            Course required =
                    store.courses().stream()
                            .filter(c -> c.getId().equals(p.getRequiredCourseId()))
                            .findFirst()
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Môn tiên quyết không tồn tại: "
                                                    + p.getRequiredCourseId()));
            require(
                    !dependsOn(required, id, new HashSet<>()),
                    "Không được tạo chu trình môn tiên quyết.");
        }
    }

    private boolean dependsOn(Course course, String target, Set<String> visited) {
        if (course.getId().equals(target)) return true;
        if (!visited.add(course.getId())) return false;
        return course.getPrerequisites().stream()
                .anyMatch(p ->
                        store.courses().stream()
                                .filter(c -> c.getId().equals(p.getRequiredCourseId()))
                                .anyMatch(c -> dependsOn(c, target, visited)));
    }

    public void saveCourse(
            Course old,
            String id,
            String name,
            int credits,
            String description,
            List<Prerequisite> prerequisites) {
        auth.requireAdmin();
        synchronized (store) {
            String key = Checks.text(id, "Mã môn");
            require(old == null || old.getId().equals(key), "Không đổi mã của môn đã tạo.");
            require(old == null || store.courses().contains(old), "Môn không tồn tại.");
            require(
                    store.courses().stream()
                            .noneMatch(c -> c != old && c.getId().equalsIgnoreCase(key)),
                    "Mã môn đã tồn tại.");
            validatePrerequisites(key, prerequisites);
            if (old == null) store.add(new Course(key, name, credits, description, prerequisites));
            else old.update(name, credits, description, prerequisites);
        }
    }

    public void deleteCourse(Course course) {
        auth.requireAdmin();
        synchronized (store) {
            require(
                    store.sections().stream().noneMatch(s -> s.getCourse() == course),
                    "Môn còn lớp học phần; không thể xóa.");
            require(
                    store.courses().stream()
                            .noneMatch(c ->
                                    c.getPrerequisites().stream()
                                            .anyMatch(p ->
                                                    p.getRequiredCourseId()
                                                            .equals(course.getId()))),
                    "Môn đang là tiên quyết của môn khác.");
            require(
                    store.users().stream()
                            .filter(Student.class::isInstance)
                            .map(Student.class::cast)
                            .noneMatch(s -> s.hasCompleted(course.getId())),
                    "Môn đang có trong kết quả hoàn thành của sinh viên.");
            store.remove(course);
        }
    }

    public void saveSection(
            CourseSection old,
            String id,
            Course course,
            Semester semester,
            Classroom room,
            String lecturer,
            int capacity,
            List<Schedule> schedules) {
        auth.requireAdmin();
        synchronized (store) {
            String key = Checks.text(id, "Mã lớp");
            require(old == null || old.getId().equals(key), "Không đổi mã của lớp đã tạo.");
            require(
                    store.courses().contains(course)
                            && store.semesters().contains(semester)
                            && store.rooms().contains(room),
                    "Môn, học kỳ hoặc phòng không tồn tại.");
            require(old == null || store.sections().contains(old), "Lớp không tồn tại.");
            require(
                    store.sections().stream()
                            .noneMatch(s -> s != old && s.getId().equalsIgnoreCase(key)),
                    "Mã lớp đã tồn tại.");
            if (old == null)
                store.add(
                        new CourseSection(
                                key, course, semester, room, lecturer, capacity, schedules));
            else {
                // History points to this section: do not rewrite its meaning after any
                // registration.
                require(
                        store.registrations().stream().noneMatch(r -> r.getSection() == old),
                        "Lớp đã có lịch sử đăng ký; giữ nguyên để bảo toàn dữ liệu.");
                old.update(course, semester, room, lecturer, capacity, schedules);
            }
        }
    }

    public void deleteSection(CourseSection section) {
        auth.requireAdmin();
        synchronized (store) {
            require(
                    store.registrations().stream().noneMatch(r -> r.getSection() == section),
                    "Lớp có lịch sử đăng ký; không thể xóa.");
            store.remove(section);
        }
    }

    public List<Student> students() {
        auth.requireAdmin();
        return store.users().stream()
                .filter(Student.class::isInstance)
                .map(Student.class::cast)
                .toList();
    }

    public void updateStudent(Student student, String name, String email, Set<String> completed) {
        auth.requireAdmin();
        synchronized (store) {
            require(store.users().contains(student), "Sinh viên không tồn tại.");
            require(
                    completed.stream()
                            .allMatch(id ->
                                    store.courses().stream().anyMatch(c -> c.getId().equals(id))),
                    "Có mã môn hoàn thành không tồn tại.");
            student.updateProfile(name, email);
            student.setCompletedCourseIds(completed);
        }
    }

    public Map<String, Integer> statistics() {
        auth.requireAdmin();
        Map<String, Integer> result = new LinkedHashMap<>();
        synchronized (store) {
            for (CourseSection section : store.sections())
                result.put(section.getId(), registrations.activeCount(section));
        }
        return Collections.unmodifiableMap(result);
    }
}
