package vn.nhom6.registration.model;

public final class Checks {
    private Checks() {}

    public static String text(String value, String label) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(label + " không được trống.");
        return value.trim();
    }

    public static int positive(int value, String label) {
        if (value <= 0) throw new IllegalArgumentException(label + " phải lớn hơn 0.");
        return value;
    }
}
