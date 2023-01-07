package dev.rdcl.www.api.auth;

import dev.rdcl.www.api.auth.fixtures.Identities;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.List;

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
    public void testConsume() {
        AuthMailService.VerificationMailEvent event = new AuthMailService.VerificationMailEvent(
            Identities.VALID_IDENTITY.getEmail(),
            "my-verification-code"
        );

        authMailService.consume(event);

        List<Mail> mails = mailbox.getMessagesSentTo(Identities.VALID_IDENTITY.getEmail());
        assertThat(mails, hasSize(1));

        Mail mail = mails.get(0);

        assertThat(extractVerificationCode(mail), is("my-verification-code"));
    }

    private String extractVerificationCode(Mail mail) {
        String html = mail.getHtml();

        int from = html.indexOf("<pre>");
        if (from == -1) {
            return null;
        }
        from += "<pre>".length();

        int to = html.indexOf("</pre>", from);
        if (to == -1) {
            return null;
        }

        return html.substring(from, to);
    }


}
