package vn.nhom6.registration.store;

import vn.nhom6.registration.model.*;

import java.util.*;

/** All service read-modify-write operations synchronize on this store. */
public final class InMemoryStore {
    private final List<User> users = new ArrayList<>();
    private final List<Course> courses = new ArrayList<>();
    private final List<CourseSection> sections = new ArrayList<>();
    private final List<Registration> registrations = new ArrayList<>();
    private final List<Semester> semesters = new ArrayList<>();
    private final List<Classroom> rooms = new ArrayList<>();

    public synchronized List<User> users() {
        return List.copyOf(users);
    }

    public synchronized List<Course> courses() {
        return List.copyOf(courses);
    }

    public synchronized List<CourseSection> sections() {
        return List.copyOf(sections);
    }

    public synchronized List<Registration> registrations() {
        return List.copyOf(registrations);
    }

    public synchronized List<Semester> semesters() {
        return List.copyOf(semesters);
    }

    public synchronized List<Classroom> rooms() {
        return List.copyOf(rooms);
    }

    public synchronized void add(User x) {
        users.add(x);
    }

    public synchronized void add(Course x) {
        courses.add(x);
    }

    public synchronized void add(CourseSection x) {
        sections.add(x);
    }

    public synchronized void add(Registration x) {
        registrations.add(x);
    }

    public synchronized void add(Semester x) {
        semesters.add(x);
    }

    public synchronized void add(Classroom x) {
        rooms.add(x);
    }

    public synchronized void remove(Course x) {
        courses.remove(x);
    }

    public synchronized void remove(CourseSection x) {
        sections.remove(x);
    }
}
