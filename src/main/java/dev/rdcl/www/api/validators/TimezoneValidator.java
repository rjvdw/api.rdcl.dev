package dev.rdcl.www.api.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.DateTimeException;
import java.time.ZoneId;

public class TimezoneValidator implements ConstraintValidator<Timezone, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        System.out.printf("Validating '%s' as a time zone", value);

        if (value == null || value.isEmpty()) {
            return true;
        }

        try {
            ZoneId.of(value);
            return true;
        } catch (DateTimeException ex) {
            return false;
        }
    }
}
