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
@Constraint(validatedBy = {JsonValidator.class})
@Documented
public @interface Json {
    String message() default "Invalid JSON";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
