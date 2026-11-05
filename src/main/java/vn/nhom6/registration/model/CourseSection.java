package vn.nhom6.registration.model;

import java.util.*;

public final class CourseSection {
    private final String id;
    private Course course;
    private Semester semester;
    private Classroom classroom;
    private String lecturer;
    private int capacity;
    private List<Schedule> schedules;

    public CourseSection(
            String id,
            Course course,
            Semester semester,
            Classroom room,
            String lecturer,
            int capacity,
            List<Schedule> schedules) {
        this.id = Checks.text(id, "Mã lớp");
        update(course, semester, room, lecturer, capacity, schedules);
    }

    public void update(
            Course course,
            Semester semester,
            Classroom room,
            String lecturer,
            int capacity,
            List<Schedule> schedules) {
        Objects.requireNonNull(course);
        Objects.requireNonNull(semester);
        Objects.requireNonNull(room);
        String validLecturer = Checks.text(lecturer, "Giảng viên");
        Checks.positive(capacity, "Sĩ số");
        if (capacity > room.getCapacity())
            throw new IllegalArgumentException("Sĩ số vượt sức chứa phòng.");
        if (schedules == null || schedules.isEmpty())
            throw new IllegalArgumentException("Cần ít nhất một buổi học.");
        // Copy meeting values so the same Schedule instance is never shared by two sections.
        List<Schedule> owned =
                schedules.stream()
                        .map(s -> new Schedule(s.getDay(), s.getStart(), s.getEnd()))
                        .toList();
        for (int i = 0; i < owned.size(); i++)
            for (int j = i + 1; j < owned.size(); j++)
                if (owned.get(i).conflictsWith(owned.get(j)))
                    throw new IllegalArgumentException("Các buổi trong lớp bị trùng nhau.");
        this.course = course;
        this.semester = semester;
        classroom = room;
        this.lecturer = validLecturer;
        this.capacity = capacity;
        this.schedules = owned;
    }

    public boolean conflictsWith(CourseSection other) {
        return semester.getId().equals(other.semester.getId())
                && schedules.stream()
                        .anyMatch(a -> other.schedules.stream().anyMatch(a::conflictsWith));
    }

    public String getId() {
        return id;
    }

    public Course getCourse() {
        return course;
    }

    public Semester getSemester() {
        return semester;
    }

    public Classroom getClassroom() {
        return classroom;
    }

    public String getLecturer() {
        return lecturer;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    @Override
    public String toString() {
        return id;
    }
}
