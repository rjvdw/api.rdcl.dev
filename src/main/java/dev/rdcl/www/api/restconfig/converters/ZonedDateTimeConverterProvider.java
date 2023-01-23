package dev.rdcl.www.api.restconfig.converters;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

@Provider
public class ZonedDateTimeConverterProvider implements ParamConverterProvider {

    private static final ParamConverter<ZonedDateTime> zonedDateTimeParamConverter = new ParamConverter<>() {
        @Override
        public ZonedDateTime fromString(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }

            try {
                return ZonedDateTime.parse(value);
            } catch (DateTimeParseException ex) {
                throw new BadRequestException("invalid zoned date time", ex);
            }
        }

        @Override
        public String toString(ZonedDateTime value) {
            return value.toOffsetDateTime().toString();
        }
    };

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType == ZonedDateTime.class) {
            //noinspection unchecked
            return (ParamConverter<T>) zonedDateTimeParamConverter;
        }
        return null;
    }
}
