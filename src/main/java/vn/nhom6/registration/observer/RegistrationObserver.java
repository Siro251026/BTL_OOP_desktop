package vn.nhom6.registration.observer;

import vn.nhom6.registration.model.Registration;

public interface RegistrationObserver {
    void onChanged(Registration registration, RegistrationEvent event);
}
