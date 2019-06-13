package com.dizzion.portal.domain.filter.persistence;

import com.dizzion.portal.domain.filter.NonFilterable;
import org.reflections.Reflections;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
public class FiltersMapper<T> {
    private final Map<String, Function<String, Object>> mapper = new HashMap<>();

    public FiltersMapper() {
        new Reflections("").getTypesAnnotatedWith(Entity.class).forEach(this::createFiltersMapper);
    }

    private void createFiltersMapper(Class clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(NonFilterable.class)) {
                continue;
            }

            if (String.class.equals(field.getType())) {
                mapper.put(buildName(clazz, field), value -> value);
            } else if (long.class.equals(field.getType())
                    || Long.class.equals(field.getType())
                    || field.isAnnotationPresent(ManyToOne.class)
                    || field.isAnnotationPresent(OneToOne.class)) {
                mapper.put(buildName(clazz, field), Long::valueOf);
            } else if (boolean.class.equals(field.getType())) {
                mapper.put(buildName(clazz, field), Boolean::valueOf);
            } else if (Integer.class.equals(field.getType())
                    || int.class.equals(field.getType())) {
                mapper.put(buildName(clazz, field), Integer::valueOf);
            } else if (LocalDate.class.equals(field.getType())) {
                mapper.put(buildName(clazz, field), LocalDate::parse);
            } else if (ZonedDateTime.class.equals(field.getType())) {
                mapper.put(buildName(clazz, field), ZonedDateTime::parse);
            } else if (field.getType().isEnum()) {
                mapper.put(buildName(clazz, field),
                        value -> getEnumValue(field.getType(), value));
            } else if (field.isAnnotationPresent(ManyToMany.class) || field.isAnnotationPresent(OneToMany.class)) {
                mapper.put(buildName(clazz, field),
                        value -> Arrays.stream(value.split(",")).map(Long::valueOf).toArray());
            } else {
                throw new BeanCreationException("Can't find filters mapping function for " +
                        clazz.getCanonicalName() + " " + field.getName());
            }
        }
    }

    private String buildName(Class clazz, Field field) {
        return buildName(clazz, field.getName());
    }

    private String buildName(Class clazz, String fieldName) {
        return clazz.getCanonicalName() + fieldName;
    }

    public Object map(Class<T> clazz, String field, String value) {
        return Optional.ofNullable(mapper.get(buildName(clazz, field)))
                .orElseThrow(NullPointerException::new)
                .apply(value);
    }

    @SuppressWarnings("unchecked")
    private Enum getEnumValue(Class<?> enumClass, String value) {
        return Enum.valueOf((Class<Enum>) enumClass, value);
    }
}
