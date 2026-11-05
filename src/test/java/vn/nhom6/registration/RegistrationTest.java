package vn.nhom6.registration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import vn.nhom6.registration.exception.RegistrationException;
import vn.nhom6.registration.model.*;
import vn.nhom6.registration.observer.*;
import vn.nhom6.registration.service.*;
import vn.nhom6.registration.store.*;
import vn.nhom6.registration.strategy.*;
import vn.nhom6.registration.ui.DesktopApp;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class RegistrationTest {
    private final Clock clock =
            Clock.fixed(Instant.parse("2026-11-05T03:00:00Z"), ZoneId.of("Asia/Bangkok"));
    private InMemoryStore store;
    private AuthService auth;
    private RegistrationService service;
    private AdminService admin;

    @BeforeEach
    void setup() {
        store = DemoData.create(clock);
        auth = new AuthService(store);
        service = new RegistrationService(store, auth, clock);
        admin = new AdminService(store, auth, service);
        login("sv01", "student123");
    }

    private void login(String user, String pass) {
        auth.login(user, pass.toCharArray());
    }

    private CourseSection section(String id) {
        return store.sections().stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private Course course(String id) {
        return store.courses().stream().filter(c -> c.getId().equals(id)).findFirst().orElseThrow();
    }

    @Test
    void registersSuccessfully() {
        int before = service.availableSeats(section("JAVA-01"));
        Registration r = service.register(section("JAVA-01"));
        assertTrue(r.isActive());
        assertEquals(before - 1, service.availableSeats(r.getSection()));
        assertEquals(LocalDateTime.now(clock), r.getRegisteredAt());
    }

    @Test
    void rejectsFullClassWithoutChangingData() {
        int before = store.registrations().size();
        assertThrows(RegistrationException.class, () -> service.register(section("DB-FULL")));
        assertEquals(before, store.registrations().size());
        assertEquals(0, service.availableSeats(section("DB-FULL")));
    }

    @Test
    void rejectsDuplicateClass() {
        service.register(section("JAVA-01"));
        assertThrows(RegistrationException.class, () -> service.register(section("JAVA-01")));
        assertEquals(1, service.activeCount(section("JAVA-01")));
    }

    @Test
    void rejectsMissingPrerequisite() {
        login("sv02", "student123");
        assertThrows(RegistrationException.class, () -> service.register(section("OOP-01")));
        assertEquals(0, service.activeCount(section("OOP-01")));
    }

    @Test
    void acceptsCompletedPrerequisite() {
        assertTrue(service.register(section("OOP-01")).isActive());
    }

    @Test
    void rejectsOverlappingSchedule() {
        service.register(section("JAVA-01"));
        assertThrows(RegistrationException.class, () -> service.register(section("MATH-01")));
        assertEquals(0, service.activeCount(section("MATH-01")));
    }

    @Test
    void acceptsAdjacentMeetings() {
        service.register(section("JAVA-01"));
        assertTrue(service.register(section("NET-01")).isActive());
    }

    @Test
    void acceptsSameTimeInDifferentSemester() {
        CourseSection original = section("JAVA-01");
        CourseSection other =
                new CourseSection(
                        "OTHER",
                        original.getCourse(),
                        section("JAVA-OLD").getSemester(),
                        original.getClassroom(),
                        "GV",
                        10,
                        original.getSchedules());
        store.add(other);
        service.register(original);
        assertTrue(service.register(other).isActive());
    }

    @Test
    void cancellationRestoresSeatAndRetainsHistory() {
        Registration r = service.register(section("JAVA-01"));
        int seats = service.availableSeats(r.getSection());
        service.drop(r);
        assertEquals(RegistrationStatus.CANCELLED, r.getStatus());
        assertEquals(seats + 1, service.availableSeats(r.getSection()));
        assertTrue(store.registrations().contains(r));
        assertEquals(LocalDateTime.now(clock), r.getCancelledAt());
    }

    @Test
    void rejectsLateCancellation() {
        Registration r =
                store.registrations().stream()
                        .filter(x -> x.getId().equals("DEMO-OLD"))
                        .findFirst()
                        .orElseThrow();
        assertThrows(RegistrationException.class, () -> service.drop(r));
        assertTrue(r.isActive());
    }

    @Test
    void rejectsCancellationByAnotherStudent() {
        Registration r = service.register(section("JAVA-01"));
        login("sv02", "student123");
        assertThrows(SecurityException.class, () -> service.drop(r));
        assertTrue(r.isActive());
    }

    @Test
    void rejectsSecondCancellation() {
        Registration r = service.register(section("JAVA-01"));
        service.drop(r);
        int seats = service.availableSeats(r.getSection());
        assertThrows(RegistrationException.class, () -> service.drop(r));
        assertEquals(seats, service.availableSeats(r.getSection()));
    }

    @Test
    void canRegisterAgainAfterCancellation() {
        Registration first = service.register(section("JAVA-01"));
        service.drop(first);
        Registration second = service.register(section("JAVA-01"));
        assertNotEquals(first.getId(), second.getId());
        assertEquals(1, service.activeCount(section("JAVA-01")));
    }

    @Test
    void searchesByCodeAndNameIgnoringCase() {
        CourseCatalogService catalog = new CourseCatalogService(store);
        assertEquals(List.of(course("OOP")), catalog.search(" oop ", new SearchByCodeStrategy()));
        assertEquals(List.of(course("DB")), catalog.search("CƠ SỞ", new SearchByNameStrategy()));
        assertTrue(catalog.search("không tồn tại", new SearchByNameStrategy()).isEmpty());
    }

    @Test
    void observersReceiveOnlySuccessfulEventsAndCanUnsubscribe() {
        List<RegistrationEvent> events = new ArrayList<>();
        RegistrationObserver o = (r, e) -> events.add(e);
        service.addObserver(o);
        Registration r = service.register(section("JAVA-01"));
        assertThrows(RegistrationException.class, () -> service.register(section("JAVA-01")));
        assertThrows(RegistrationException.class, () -> service.register(section("DB-FULL")));
        service.drop(r);
        assertThrows(RegistrationException.class, () -> service.drop(r));
        assertEquals(List.of(RegistrationEvent.REGISTERED, RegistrationEvent.CANCELLED), events);
        service.removeObserver(o);
        service.register(section("JAVA-01"));
        assertEquals(2, events.size());
    }

    @Test
    void concreteObserversWork() {
        List<String> notifications = new ArrayList<>();
        AtomicInteger refreshes = new AtomicInteger();
        service.addObserver(new NotificationObserver(notifications::add));
        service.addObserver(new StatisticsObserver(refreshes::incrementAndGet));
        Registration r = service.register(section("JAVA-01"));
        service.drop(r);
        assertEquals(2, refreshes.get());
        assertTrue(notifications.get(0).contains("JAVA-01"));
        assertTrue(notifications.get(1).contains("hủy"));
    }

    @Test
    void brokenObserverDoesNotUndoCommittedRegistration() {
        AtomicInteger calls = new AtomicInteger();
        service.addObserver((r, e) -> {
            throw new IllegalStateException("test observer");
        });
        service.addObserver((r, e) -> calls.incrementAndGet());
        assertTrue(service.register(section("JAVA-01")).isActive());
        assertEquals(1, calls.get());
    }

    @Test
    void studentCannotPerformAnyAdminOperation() {
        Student student = auth.requireStudent();
        assertAll(
                () -> assertThrows(SecurityException.class, admin::students),
                () -> assertThrows(SecurityException.class, admin::statistics),
                () ->
                        assertThrows(
                                SecurityException.class, () -> admin.deleteCourse(course("NET"))),
                () ->
                        assertThrows(
                                SecurityException.class,
                                () -> admin.deleteSection(section("NET-01"))),
                () ->
                        assertThrows(
                                SecurityException.class,
                                () -> admin.saveCourse(null, "NEW", "New", 3, "", List.of())),
                () ->
                        assertThrows(
                                SecurityException.class,
                                () -> admin.updateStudent(student, "X", "x@test", Set.of())),
                () ->
                        assertThrows(
                                SecurityException.class,
                                () ->
                                        admin.saveSection(
                                                null,
                                                "X",
                                                course("NET"),
                                                section("NET-01").getSemester(),
                                                section("NET-01").getClassroom(),
                                                "GV",
                                                1,
                                                section("NET-01").getSchedules())));
    }

    @Test
    void anonymousCannotRegisterDropOrReadHistory() {
        Registration r = service.register(section("JAVA-01"));
        auth.logout();
        assertThrows(SecurityException.class, () -> service.register(section("NET-01")));
        assertThrows(SecurityException.class, () -> service.drop(r));
        assertThrows(SecurityException.class, service::visibleRegistrations);
    }

    @Test
    void adminCannotRegisterAsStudent() {
        login("admin", "admin123");
        assertThrows(SecurityException.class, () -> service.register(section("JAVA-01")));
    }

    @Test
    void lastSeatIsAtomicAcrossServices() throws Exception {
        CourseSection base = section("NET-01");
        CourseSection last =
                new CourseSection(
                        "LAST",
                        base.getCourse(),
                        base.getSemester(),
                        base.getClassroom(),
                        "GV",
                        1,
                        base.getSchedules());
        store.add(last);
        AuthService otherAuth = new AuthService(store);
        otherAuth.login("sv02", "student123".toCharArray());
        RegistrationService otherService = new RegistrationService(store, otherAuth, clock);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (RegistrationService s : List.of(service, otherService))
                futures.add(
                        pool.submit(() -> {
                            try {
                                start.await();
                                s.register(last);
                                success.incrementAndGet();
                            } catch (RegistrationException expected) {
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                                throw new RuntimeException(ex);
                            }
                        }));
            start.countDown();
            for (Future<?> f : futures) f.get(5, TimeUnit.SECONDS);
            assertEquals(1, success.get());
            assertEquals(1, service.activeCount(last));
            assertEquals(0, service.availableSeats(last));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void adminCrudPreservesReferencesAndHistory() {
        login("admin", "admin123");
        admin.saveCourse(null, "NEW", "Môn mới", 2, "demo", List.of());
        Course c = course("NEW");
        admin.saveCourse(c, "NEW", "Môn đã sửa", 3, "", List.of(new Prerequisite("JAVA")));
        assertEquals("Môn đã sửa", c.getName());
        assertEquals(3, c.getCredits());
        CourseSection base = section("NET-01");
        admin.saveSection(
                null,
                "NEW-01",
                c,
                base.getSemester(),
                base.getClassroom(),
                "GV",
                2,
                base.getSchedules());
        assertThrows(IllegalArgumentException.class, () -> admin.deleteCourse(c));
        admin.deleteSection(section("NEW-01"));
        admin.deleteCourse(c);
        assertFalse(store.courses().contains(c));
        assertThrows(IllegalArgumentException.class, () -> admin.deleteSection(section("DB-FULL")));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        admin.saveSection(
                                section("DB-FULL"),
                                "DB-FULL",
                                c,
                                base.getSemester(),
                                base.getClassroom(),
                                "GV",
                                2,
                                base.getSchedules()));
    }

    @Test
    void invalidCourseEditIsAtomic() {
        login("admin", "admin123");
        Course c = course("JAVA");
        String oldName = c.getName();
        assertThrows(
                IllegalArgumentException.class,
                () -> admin.saveCourse(c, "JAVA", "Changed", 0, "", List.of()));
        assertEquals(oldName, c.getName());
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        admin.saveCourse(
                                c, "JAVA", "Changed", 3, "", List.of(new Prerequisite("OOP"))));
        assertEquals(oldName, c.getName());
    }

    @Test
    void adminCanUpdateStudentAndViewStatistics() {
        login("admin", "admin123");
        Student s = admin.students().get(1);
        admin.updateStudent(s, "Bình mới", "binh@example.test", Set.of("JAVA"));
        assertTrue(s.hasCompleted("JAVA"));
        assertEquals(1, admin.statistics().get("DB-FULL"));
        assertEquals(2, service.visibleRegistrations().size());
    }

    @Test
    void accountCreationAndFailedLoginDoNotGrantPrivileges() {
        Student newStudent =
                auth.register("newuser", "abcdef".toCharArray(), "New", "new@example.test");
        assertEquals(UserRole.STUDENT, newStudent.getRole());
        assertThrows(
                IllegalArgumentException.class,
                () -> auth.register("NEWUSER", "abcdef".toCharArray(), "New", "x@test"));
        assertThrows(SecurityException.class, () -> auth.login("admin", "badpass".toCharArray()));
        assertThrows(SecurityException.class, auth::currentUser);
        assertEquals(newStudent, auth.login("newuser", "abcdef".toCharArray()));
        assertThrows(IllegalArgumentException.class, () -> new Account("x", "123".toCharArray()));
    }

    @Test
    void collectionViewsAreProtectedAndSchedulesAreOwned() {
        assertThrows(UnsupportedOperationException.class, () -> store.courses().clear());
        assertThrows(
                UnsupportedOperationException.class,
                () -> auth.requireStudent().getCompletedCourseIds().clear());
        CourseSection base = section("NET-01");
        Schedule meeting = base.getSchedules().get(0);
        CourseSection other =
                new CourseSection(
                        "OWNED",
                        base.getCourse(),
                        base.getSemester(),
                        base.getClassroom(),
                        "GV",
                        1,
                        List.of(meeting));
        assertNotSame(meeting, other.getSchedules().get(0));
        assertThrows(UnsupportedOperationException.class, () -> other.getSchedules().clear());
    }

    @Test
    void multipleMeetingsAndSundayParseCorrectly() {
        List<Schedule> schedules = DesktopApp.parseSchedules("2 08:00-10:00; 8 13:00-15:00");
        assertEquals(2, schedules.size());
        assertEquals(DayOfWeek.SUNDAY, schedules.get(1).getDay());
        assertThrows(IllegalArgumentException.class, () -> DesktopApp.parseSchedules("wrong"));
        assertThrows(
                IllegalArgumentException.class, () -> DesktopApp.parseSchedules("2 10:00-08:00"));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Schedule(DayOfWeek.MONDAY, LocalTime.NOON, LocalTime.NOON));
    }

    @Test
    void dropDeadlineIncludesFinalDay() {
        Semester semester =
                new Semester(
                        "BOUNDARY",
                        LocalDate.of(2026, 11, 1),
                        LocalDate.of(2026, 12, 1),
                        LocalDate.of(2026, 11, 5));
        assertTrue(semester.canDrop(LocalDate.of(2026, 11, 5)));
        assertFalse(semester.canDrop(LocalDate.of(2026, 11, 6)));
    }

    @Test
    void adminCanCancelForStudentWithinDeadline() {
        Registration r = service.register(section("JAVA-01"));
        login("admin", "admin123");
        service.drop(r);
        assertFalse(r.isActive());
    }
}
