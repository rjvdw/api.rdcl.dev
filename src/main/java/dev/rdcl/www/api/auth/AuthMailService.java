package dev.rdcl.www.api.auth;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.ws.rs.core.UriBuilder;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class AuthMailService {

    private final AuthProperties authProperties;

    private final Mailer mailer;

    private final Event<VerificationMailEvent> verificationMailEvent;

    public void sendVerificationMail(String recipient, String verificationCode, URI callback) {
        VerificationMailEvent event = new VerificationMailEvent(recipient, verificationCode, callback);
        verificationMailEvent.fire(event);
    }

    public void consume(@Observes VerificationMailEvent event) {
        String recipient = event.recipient();
        URI callback = buildCallback(event.callback(), event.verificationCode());

        Mail mail = new Mail();

        mail.setFrom(authProperties.verificationEmailFrom());
        mail.setTo(List.of(recipient));
        mail.setSubject("Login request");
        mail.setText("Go to %s to verify your login attempt.".formatted(callback));
        mail.setHtml("""
            <p>
                <a href="%s">Click here to verify your login attempt.</a>
            </p>
            """.formatted(callback));

        mailer.send(mail);
    }

    private URI buildCallback(URI baseUri, String verificationCode) {
        return UriBuilder.fromUri(baseUri == null ? defaultCallback() : baseUri)
            .queryParam("verification-code", verificationCode)
            .build();
    }

    private URI defaultCallback() {
        return URI.create(authProperties.defaultLoginCallbackUrl());
    }

    public record VerificationMailEvent(String recipient, String verificationCode, URI callback) {
    }

}
