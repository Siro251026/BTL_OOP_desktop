package vn.nhom6.registration.observer;

import vn.nhom6.registration.model.Registration;

public final class StatisticsObserver implements RegistrationObserver {
    private final Runnable refresh;

    public StatisticsObserver(Runnable refresh) {
        this.refresh = refresh;
    }

    @Override
    public void onChanged(Registration r, RegistrationEvent event) {
        refresh.run();
    }
}
