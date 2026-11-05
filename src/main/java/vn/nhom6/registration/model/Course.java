package vn.nhom6.registration.model;

import java.util.List;

public final class Course {
    private final String id;
    private String name;
    private int credits;
    private String description;
    private List<Prerequisite> prerequisites;

    public Course(
            String id,
            String name,
            int credits,
            String description,
            List<Prerequisite> prerequisites) {
        this.id = Checks.text(id, "Mã môn");
        update(name, credits, description, prerequisites);
    }

    public void update(
            String name, int credits, String description, List<Prerequisite> prerequisites) {
        String validName = Checks.text(name, "Tên môn");
        int validCredits = Checks.positive(credits, "Tín chỉ");
        List<Prerequisite> validPrerequisites = List.copyOf(prerequisites);
        this.name = validName;
        this.credits = validCredits;
        this.description = description == null ? "" : description.trim();
        this.prerequisites = validPrerequisites;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCredits() {
        return credits;
    }

    public String getDescription() {
        return description;
    }

    public List<Prerequisite> getPrerequisites() {
        return prerequisites;
    }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}
