package vn.nhom6.registration.model;

import java.time.*;
import java.util.Objects;

/** A section owns its own immutable meeting instances. */
public final class Schedule {
    private final DayOfWeek day;
    private final LocalTime start;
    private final LocalTime end;

    public Schedule(DayOfWeek day, LocalTime start, LocalTime end) {
        this.day = Objects.requireNonNull(day);
        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);
        if (!start.isBefore(end))
            throw new IllegalArgumentException("Giờ bắt đầu phải trước giờ kết thúc.");
    }

    public boolean conflictsWith(Schedule other) {
        return day == other.day && start.isBefore(other.end) && other.start.isBefore(end);
    }

    public DayOfWeek getDay() {
        return day;
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "T" + (day.getValue() + 1) + " " + start + "-" + end;
    }
}
