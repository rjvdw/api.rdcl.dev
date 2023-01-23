package dev.rdcl.www.api.restconfig.converters;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Provider
public class LocalDateConverterProvider implements ParamConverterProvider {

    private static final ParamConverter<LocalDate> localDateConverter = new ParamConverter<>() {
        @Override
        public LocalDate fromString(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }

            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException ex) {
                throw new BadRequestException("invalid date", ex);
            }
        }

        @Override
        public String toString(LocalDate value) {
            return value.toString();
        }
    };

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType == LocalDate.class) {
            //noinspection unchecked
            return (ParamConverter<T>) localDateConverter;
        }
        return null;
    }
}
