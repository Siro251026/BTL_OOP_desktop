package vn.nhom6.registration.model;

public final class Prerequisite {
    private final String requiredCourseId;

    public Prerequisite(String id) {
        requiredCourseId = Checks.text(id, "Mã môn tiên quyết");
    }

    public String getRequiredCourseId() {
        return requiredCourseId;
    }

    public boolean isSatisfiedBy(Student student) {
        return student.hasCompleted(requiredCourseId);
    }
}
