package dev.rdcl.www.api.auth;

import dev.rdcl.www.api.auth.fixtures.Identities;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.rdcl.www.api.auth.TestUtils.extractVerificationCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class AuthMailServiceTest {

    @Inject
    MockMailbox mailbox;

    @Inject
    AuthMailService authMailService;

    @BeforeEach
    public void setup() {
        mailbox.clear();
    }

    @Test
    @DisplayName("A mail with a verification code is sent when a user tries to log in")
    public void testSendVerificationMail() {
        authMailService.sendVerificationMail(
                Identities.VALID_IDENTITY.getEmail(),
                "my-verification-code",
                null
        );

        List<Mail> mails = mailbox.getMailsSentTo(Identities.VALID_IDENTITY.getEmail());
        assertThat(mails, hasSize(1));

        Mail mail = mails.get(0);

        assertThat(extractVerificationCode(mail), is("my-verification-code"));
    }

}
