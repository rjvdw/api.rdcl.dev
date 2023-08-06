package dev.rdcl.www.api.auth.events;

public sealed interface InitiateLoginAttemptCompleteEvent
    permits LoginAttemptInitiatedEvent, LoginAttemptAbortedEvent {

    boolean success();

    static InitiateLoginAttemptCompleteEvent initiated() {
        return new LoginAttemptInitiatedEvent();
    }

    static InitiateLoginAttemptCompleteEvent aborted() {
        return new LoginAttemptAbortedEvent();
    }
}

record LoginAttemptAbortedEvent() implements InitiateLoginAttemptCompleteEvent {
    @Override
    public boolean success() {
        return false;
    }
}

record LoginAttemptInitiatedEvent() implements InitiateLoginAttemptCompleteEvent {
    @Override
    public boolean success() {
        return true;
    }
}
