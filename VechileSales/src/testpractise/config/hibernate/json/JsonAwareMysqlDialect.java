package com.dizzion.portal.config.hibernate.json;

import org.hibernate.dialect.MySQL57InnoDBDialect;

import java.sql.Types;

public class JsonAwareMysqlDialect extends MySQL57InnoDBDialect {
    public JsonAwareMysqlDialect() {
        super();
        this.registerColumnType(Types.JAVA_OBJECT, "json");
    }
}
