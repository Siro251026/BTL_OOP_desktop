package vn.nhom6.registration.model;

import java.util.Set;

public final class Student extends User {
    private Set<String> completedCourseIds;

    public Student(String id, String name, String email, Account account, Set<String> completed) {
        super(id, name, email, account);
        setCompletedCourseIds(completed);
    }

    public void setCompletedCourseIds(Set<String> ids) {
        completedCourseIds = Set.copyOf(ids);
    }

    public Set<String> getCompletedCourseIds() {
        return completedCourseIds;
    }

    public boolean hasCompleted(String courseId) {
        return completedCourseIds.contains(courseId);
    }

    @Override
    public UserRole getRole() {
        return UserRole.STUDENT;
    }
}
