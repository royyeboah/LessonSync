package com.adbdti.lessonsync.Auth;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoogleSessionServiceTest {

    private final MockHttpSession session = new MockHttpSession();
    private final GoogleSessionService service = new GoogleSessionService(session);

    @Test
    void acceptsTheStateItIssued() {
        service.storeState("abc123");

        assertThat(service.consumeState("abc123")).isTrue();
    }

    @Test
    void rejectsTheSameStateASecondTime() {
        service.storeState("abc123");
        service.consumeState("abc123");

        assertThat(service.consumeState("abc123")).isFalse();
    }

    @Test
    void rejectsAStateItDidNotIssue() {
        service.storeState("abc123");

        assertThat(service.consumeState("forged")).isFalse();
    }

    @Test
    void rejectsAMissingState() {
        service.storeState("abc123");

        assertThat(service.consumeState(null)).isFalse();
    }

    @Test
    void rejectsAnyStateWhenNoneWasIssued() {
        assertThat(service.consumeState("abc123")).isFalse();
    }

    @Test
    void remembersTheConnectedAccount() {
        service.setAccount(new GoogleAccount("user-1", "student@example.com"));

        assertThat(service.getUserId()).isEqualTo("user-1");
        assertThat(service.getEmail()).isEqualTo("student@example.com");
        assertThat(service.requireUserId()).isEqualTo("user-1");
    }

    @Test
    void forgetsTheAccountOnDisconnect() {
        service.setAccount(new GoogleAccount("user-1", "student@example.com"));
        service.clearAccount();

        assertThat(service.getUserId()).isNull();
        assertThat(service.getEmail()).isNull();
    }

    @Test
    void requireUserIdFailsWhenNotConnected() {
        assertThatThrownBy(service::requireUserId)
                .isInstanceOf(GoogleAuthRequiredException.class);
    }
}
