package vn.nhom6.registration.strategy;

import vn.nhom6.registration.model.Course;

public interface CourseSearchStrategy {
    boolean matches(Course course, String keyword);
}
