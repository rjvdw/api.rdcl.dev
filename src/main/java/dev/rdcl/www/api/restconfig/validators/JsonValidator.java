package dev.rdcl.www.api.restconfig.validators;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class JsonValidator implements ConstraintValidator<Json, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }

        try {
            JsonParser.parseString(value);
            return true;
        } catch (JsonParseException ex) {
            return false;
        }
    }
}
