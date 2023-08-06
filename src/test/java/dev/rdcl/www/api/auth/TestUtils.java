package dev.rdcl.www.api.auth;

import io.quarkus.mailer.Mail;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestUtils {

    public static String extractVerificationCode(Mail mail) {
        String html = mail.getHtml();

        int from = html.indexOf("verification-code=");
        if (from == -1) {
            return null;
        }
        from += "verification-code=".length();

        int to = html.indexOf("\"", from);
        if (to == -1) {
            return null;
        }

        return html.substring(from, to);
    }

}
