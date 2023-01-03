package dev.rdcl.www.api.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {IsoDateTimeValidator.class})
@Documented
public @interface IsoDateTime {
    String message() default "Invalid date-time";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
