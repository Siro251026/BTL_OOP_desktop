package vn.nhom6.registration.service;

import vn.nhom6.registration.model.*;
import vn.nhom6.registration.store.InMemoryStore;
import vn.nhom6.registration.strategy.CourseSearchStrategy;

import java.util.*;

public final class CourseCatalogService {
    private final InMemoryStore store;

    public CourseCatalogService(InMemoryStore store) {
        this.store = store;
    }

    public List<Course> search(String keyword, CourseSearchStrategy strategy) {
        Objects.requireNonNull(strategy);
        synchronized (store) {
            return store.courses().stream().filter(c -> strategy.matches(c, keyword)).toList();
        }
    }

    public List<CourseSection> sectionsFor(Course course) {
        return store.sections().stream().filter(s -> s.getCourse() == course).toList();
    }
}
