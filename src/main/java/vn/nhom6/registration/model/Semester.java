package vn.nhom6.registration.model;

import java.time.LocalDate;
import java.util.Objects;

public final class Semester {
    private final String id;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalDate dropDeadline;

    public Semester(String id, LocalDate start, LocalDate end, LocalDate deadline) {
        this.id = Checks.text(id, "Học kỳ");
        startDate = Objects.requireNonNull(start);
        endDate = Objects.requireNonNull(end);
        dropDeadline = Objects.requireNonNull(deadline);
        if (end.isBefore(start) || deadline.isBefore(start) || deadline.isAfter(end))
            throw new IllegalArgumentException("Ngày học kỳ/hạn hủy không hợp lệ.");
    }

    public boolean canDrop(LocalDate today) {
        return !today.isAfter(dropDeadline);
    }

    public String getId() {
        return id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDate getDropDeadline() {
        return dropDeadline;
    }

    @Override
    public String toString() {
        return id;
    }
}
