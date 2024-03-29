package dev.rdcl.www.api.restconfig.converters;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeParseException;

@Provider
public class InstantConverterProvider implements ParamConverterProvider {

    private static final ParamConverter<Instant> instantConverter = new ParamConverter<>() {
        @Override
        public Instant fromString(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }

            try {
                return Instant.parse(value);
            } catch (DateTimeParseException ex) {
                throw new BadRequestException("invalid instant", ex);
            }
        }

        @Override
        public String toString(Instant value) {
            return value.toString();
        }
    };

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType == Instant.class) {
            //noinspection unchecked
            return (ParamConverter<T>) instantConverter;
        }
        return null;
    }
}
