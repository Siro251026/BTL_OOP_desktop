package vn.nhom6.registration.store;

import vn.nhom6.registration.model.*;

import java.time.*;
import java.util.*;

public final class DemoData {
    private DemoData() {}

    public static InMemoryStore create(Clock clock) {
        InMemoryStore store = new InMemoryStore();
        LocalDate today = LocalDate.now(clock);
        Semester semester =
                new Semester(
                        "HK1-DEMO", today.minusDays(30), today.plusDays(120), today.plusDays(30));
        Semester expired =
                new Semester("HK-CU", today.minusDays(150), today.plusDays(5), today.minusDays(1));
        store.add(semester);
        store.add(expired);
        Classroom room = new Classroom("A2-301", 60);
        store.add(room);
        store.add(new Classroom("A2-302", 40));
        Student an =
                new Student(
                        "SV01",
                        "Sinh viên An",
                        "an@example.test",
                        new Account("sv01", "student123".toCharArray()),
                        Set.of("JAVA"));
        Student binh =
                new Student(
                        "SV02",
                        "Sinh viên Bình",
                        "binh@example.test",
                        new Account("sv02", "student123".toCharArray()),
                        Set.of());
        store.add(an);
        store.add(binh);
        store.add(
                new Admin(
                        "AD01",
                        "Quản trị demo",
                        "admin@example.test",
                        new Account("admin", "admin123".toCharArray())));
        Course java =
                new Course(
                        "JAVA",
                        "Lập trình Java cơ bản",
                        3,
                        "Biến, điều khiển, lớp và đối tượng",
                        List.of());
        Course oop =
                new Course(
                        "OOP",
                        "Lập trình hướng đối tượng",
                        3,
                        "Kế thừa, đa hình, SOLID và patterns",
                        List.of(new Prerequisite("JAVA")));
        Course math = new Course("MATH", "Toán rời rạc", 3, "Logic và tập hợp", List.of());
        Course db = new Course("DB", "Cơ sở dữ liệu", 3, "Mô hình quan hệ", List.of());
        Course net = new Course("NET", "Mạng máy tính", 3, "Kiến thức mạng cơ bản", List.of());
        for (Course c : List.of(java, oop, math, db, net)) store.add(c);
        CourseSection full = section("DB-FULL", db, semester, room, 1, DayOfWeek.WEDNESDAY, 8, 10);
        CourseSection old = section("JAVA-OLD", java, expired, room, 20, DayOfWeek.FRIDAY, 8, 10);
        for (CourseSection s :
                List.of(
                        section("JAVA-01", java, semester, room, 30, DayOfWeek.MONDAY, 8, 10),
                        section("OOP-01", oop, semester, room, 30, DayOfWeek.TUESDAY, 8, 10),
                        section("MATH-01", math, semester, room, 30, DayOfWeek.MONDAY, 9, 11),
                        section("NET-01", net, semester, room, 30, DayOfWeek.MONDAY, 10, 12),
                        full,
                        old)) store.add(s);
        store.add(new Registration("DEMO-FULL", binh, full, LocalDateTime.now(clock)));
        store.add(new Registration("DEMO-OLD", an, old, LocalDateTime.now(clock)));
        return store;
    }

    private static CourseSection section(
            String id,
            Course c,
            Semester semester,
            Classroom room,
            int capacity,
            DayOfWeek day,
            int start,
            int end) {
        return new CourseSection(
                id,
                c,
                semester,
                room,
                "Giảng viên demo",
                capacity,
                List.of(new Schedule(day, LocalTime.of(start, 0), LocalTime.of(end, 0))));
    }
}
