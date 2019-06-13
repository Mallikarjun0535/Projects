package com.dizzion.portal.config.hibernate.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.DynamicParameterizedType;
import org.hibernate.usertype.UserType;
import org.springframework.core.ResolvableType;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

@Slf4j
public class JsonUserType implements UserType, DynamicParameterizedType {

    private static final int JAVA_SQL_TYPE = Types.JAVA_OBJECT;

    private JavaType returnedType;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public int[] sqlTypes() {
        return new int[]{JAVA_SQL_TYPE};
    }

    @Override
    public Class returnedClass() {
        return returnedType.getRawClass();
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return Objects.hashCode(x);
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
            throws HibernateException, SQLException {
        String columnName = names[0];
        String columnValue = (String) rs.getObject(columnName);
        log.debug("Result set column {0} value is {1}", columnName, columnValue);
        try {
            return columnValue == null
                    ? null
                    : objectMapper.readValue(columnValue, returnedType);
        } catch (IOException e) {
            throw new HibernateException(e);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
            throws HibernateException, SQLException {
        if (value == null) {
            log.debug("Binding null to parameter {0} ", index);
            st.setNull(index, JAVA_SQL_TYPE);
        } else {
            try {
                String stringValue = objectMapper.writeValueAsString(value);
                log.debug("Binding {0} to parameter {1} ", stringValue, index);
                st.setString(index, stringValue);
            } catch (JsonProcessingException e) {
                throw new HibernateException(e);
            }
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        try {
            return value == null
                    ? null
                    : objectMapper.readValue(objectMapper.writeValueAsString(value), returnedType);
        } catch (IOException e) {
            throw new HibernateException(e);
        }
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) deepCopy(value);
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return deepCopy(cached);
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return deepCopy(original);
    }

    @Override
    public void setParameterValues(Properties parameters) {
        String entityClassName = (String) parameters.get(ENTITY);
        String fieldName = (String) parameters.get(PROPERTY);
        try {
            Class<?> entityClass = Class.forName(entityClassName);
            Field field = entityClass.getDeclaredField(fieldName);
            this.returnedType = resolveJacksonType(field);
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new HibernateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private JavaType resolveJacksonType(Field field) {
        ResolvableType type = ResolvableType.forField(field);
        Class clazz = type.getRawClass();
        if (Collection.class.isAssignableFrom(clazz)) {
            Class<?> elemClass = type.resolveGeneric(0);
            return objectMapper.getTypeFactory().constructCollectionType(clazz, elemClass);
        } else if (Map.class.isAssignableFrom(clazz)) {
            Class<?> keyClass = type.resolveGeneric(0);
            Class<?> valueClass = type.resolveGeneric(1);
            return objectMapper.getTypeFactory().constructMapType(clazz, keyClass, valueClass);
        } else {
            return objectMapper.constructType(type.getClass());
        }
    }
}
