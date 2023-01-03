package dev.rdcl.www.api.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.function.Supplier;

public class IsoDateTimeValidator implements ConstraintValidator<IsoDateTime, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }

        try {
            ZonedDateTime.parse(value);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    public static ZonedDateTime parse(String value) {
        return parse(value, () -> null);
    }

    public static ZonedDateTime parse(String value, ZonedDateTime defaultVAlue) {
        return parse(value, () -> defaultVAlue);
    }

    public static ZonedDateTime parse(String value, Supplier<ZonedDateTime> defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue.get();
        }

        return ZonedDateTime.parse(value);
    }
}
