package vn.nhom6.registration.model;

public final class Classroom {
    private final String id;
    private final int capacity;

    public Classroom(String id, int capacity) {
        this.id = Checks.text(id, "Phòng");
        this.capacity = Checks.positive(capacity, "Sức chứa phòng");
    }

    public String getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public String toString() {
        return id;
    }
}
