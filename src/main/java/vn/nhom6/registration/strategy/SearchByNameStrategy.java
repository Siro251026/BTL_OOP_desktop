package vn.nhom6.registration.strategy;

import vn.nhom6.registration.model.Course;

import java.util.Locale;

public final class SearchByNameStrategy implements CourseSearchStrategy {
    @Override
    public boolean matches(Course course, String keyword) {
        return course.getName()
                .toLowerCase(Locale.ROOT)
                .contains(keyword.trim().toLowerCase(Locale.ROOT));
    }
}
