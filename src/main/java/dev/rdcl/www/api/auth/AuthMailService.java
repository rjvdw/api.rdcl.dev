package dev.rdcl.www.api.auth;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import lombok.RequiredArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class AuthMailService {

    private final Mailer mailer;

    private final Event<VerificationMailEvent> verificationMailEvent;

    public void sendVerificationMail(String recipient, String verificationCode) {
        VerificationMailEvent event = new VerificationMailEvent(recipient, verificationCode);
        verificationMailEvent.fire(event);
    }

    public void consume(@Observes VerificationMailEvent event) {
        String recipient = event.recipient();
        String verificationCode = event.verificationCode();

        Mail mail = new Mail();

        mail.setFrom("noreply@rdcl.dev");
        mail.setTo(List.of(recipient));
        mail.setSubject("Login request");
        mail.setText("Verification code: %s".formatted(verificationCode));
        mail.setHtml("<p>Verification code: <pre>%s</pre></code>".formatted(verificationCode));

        mailer.send(mail);
    }

    public record VerificationMailEvent(String recipient, String verificationCode) {
    }

}
