package dev.rdcl.www.api.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.function.Supplier;

public class IsoInstantValidator implements ConstraintValidator<IsoInstant, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }

        try {
            parse(value);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    public static Instant parse(String value) {
        return parse(value, () -> null);
    }

    public static Instant parse(String value, Instant defaultVAlue) {
        return parse(value, () -> defaultVAlue);
    }

    public static Instant parse(String value, Supplier<Instant> defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue.get();
        }

        return Instant.parse(value);
    }
}
