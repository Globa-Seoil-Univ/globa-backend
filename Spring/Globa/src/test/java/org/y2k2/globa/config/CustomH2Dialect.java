package org.y2k2.globa.config;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.type.SqlTypes;

public class CustomH2Dialect extends H2Dialect {
    @Override
    protected String columnType(int sqlTypeCode) {
        return switch (sqlTypeCode) {
            case SqlTypes.CLOB, SqlTypes.LONGVARCHAR -> "text";
            default -> super.columnType(sqlTypeCode);
        };
    }
}
