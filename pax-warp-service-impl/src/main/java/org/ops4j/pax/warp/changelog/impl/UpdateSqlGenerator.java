/*
 * Copyright 2014 Harald Wellmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.warp.changelog.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Consumer;

import org.ops4j.pax.warp.jaxb.AddForeignKey;
import org.ops4j.pax.warp.jaxb.AddPrimaryKey;
import org.ops4j.pax.warp.jaxb.ChangeSet;
import org.ops4j.pax.warp.jaxb.ColumnValue;
import org.ops4j.pax.warp.jaxb.CreateTable;
import org.ops4j.pax.warp.jaxb.DropForeignKey;
import org.ops4j.pax.warp.jaxb.DropPrimaryKey;
import org.ops4j.pax.warp.jaxb.Insert;
import org.ops4j.pax.warp.jaxb.TruncateTable;
import org.ops4j.pax.warp.jaxb.visitor.VisitorAction;
import org.ops4j.pax.warp.util.Exceptions;


public class UpdateSqlGenerator extends AbstractSqlGenerator {


    public UpdateSqlGenerator(String dbms, Connection dbc, Consumer<PreparedStatement> consumer) {
        super(dbms, dbc, consumer);
    }

    @Override
    public VisitorAction enter(CreateTable action) {
        return produceStatement("createTable", action);
    }

    @Override
    public VisitorAction enter(AddPrimaryKey action) {
        return produceStatement("addPrimaryKey", action);
    }

    @Override
    public VisitorAction enter(AddForeignKey action) {
        return produceStatement("addForeignKey", action);
    }

    @Override
    public VisitorAction enter(DropForeignKey action) {
        return produceStatement("dropForeignKey", action);
    }

    @Override
    public VisitorAction enter(DropPrimaryKey action) {
        return produceStatement("dropPrimaryKey", action);
    }

    @Override
    public VisitorAction enter(Insert action) {
        return generateInsert(action);
    }

    @Override
    public VisitorAction enter(TruncateTable action) {
        return produceStatement("truncateTable", action);
    }

    @Override
    public VisitorAction leave(ChangeSet aBean) {
        try {
            dbc.commit();
        }
        catch (SQLException exc) {
            throw Exceptions.unchecked(exc);
        }
        return VisitorAction.CONTINUE;
    }

    protected VisitorAction generateInsert(Insert action) {
        String rawSql = renderTemplate("insert", action);
        try (PreparedStatement st = dbc.prepareStatement(rawSql)) {
            int i = 1;
            for (ColumnValue columnValue : action.getColumn()) {
                JDBCType jdbcType = JDBCType.valueOf(columnValue.getType());
                Object value = convertValue(columnValue);
                if (value == null) {
                    st.setNull(i, jdbcType.getVendorTypeNumber());
                }
                st.setObject(i, value);
                i++;
            }
            consumer.accept(st);
        }
        catch (SQLException exc) {
            throw Exceptions.unchecked(exc);
        }
        return VisitorAction.SKIP;
    }

    /**
     * @param columnValue
     * @return
     */
    private Object convertValue(ColumnValue columnValue) {
        JDBCType jdbcType = JDBCType.valueOf(columnValue.getType());
        String value = columnValue.getValue();
        switch (jdbcType) {
            case BIGINT:
                return Long.parseLong(value);
            case BIT:
                return Boolean.parseBoolean(value);
            case CHAR:
            case CLOB:
            case VARCHAR:
            case LONGVARCHAR:
                return value;
            case DECIMAL:
            case NUMERIC:
                return new BigDecimal(value);
            case INTEGER:
                return Integer.parseInt(value);
            case SMALLINT:
                return Short.parseShort(value);
            case TINYINT:
                return Byte.parseByte(value);
            default:
                return null;
        }
    }
}
