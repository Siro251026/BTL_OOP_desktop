package vn.nhom6.registration.observer;

import vn.nhom6.registration.model.Registration;

import java.util.function.Consumer;

public final class NotificationObserver implements RegistrationObserver {
    private final Consumer<String> output;

    public NotificationObserver(Consumer<String> output) {
        this.output = output;
    }

    @Override
    public void onChanged(Registration r, RegistrationEvent event) {
        output.accept(
                (event == RegistrationEvent.REGISTERED ? "Đã đăng ký " : "Đã hủy ")
                        + r.getSection().getId()
                        + " - "
                        + r.getStudent().getId());
    }
}
