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
        return renderTemplate("createTable", action);
    }

    @Override
    public VisitorAction enter(AddPrimaryKey action) {
        return renderTemplate("addPrimaryKey", action);
    }

    @Override
    public VisitorAction enter(AddForeignKey action) {
        return renderTemplate("addForeignKey", action);
    }
    
    @Override
    public VisitorAction enter(DropForeignKey action) {
        return renderTemplate("dropForeignKey", action);
    }
    
    @Override
    public VisitorAction enter(DropPrimaryKey action) {
        return renderTemplate("dropPrimaryKey", action);
    }
    
    @Override
    public VisitorAction enter(Insert action) {
        return generateInsert(action);
    }
    
    @Override
    public VisitorAction enter(TruncateTable action) {
        return renderTemplate("truncateTable", action);
    }
    
    protected VisitorAction generateInsert(Insert action) {
        int numColumns = action.getColumn().size();
        StringBuilder builder = new StringBuilder("INSERT INTO ");
        builder.append(action.getTableName());
        builder.append(" (");
        boolean first = true;
        for (ColumnValue columnValue : action.getColumn()) {
            if (first) {
                first = false;
            }
            else {
                builder.append(", ");
            }
            builder.append(columnValue.getName());
        }
        builder.append(") VALUES (? ");
        for (int i = 1; i < numColumns; i++) {
            builder.append(", ?");
        }
        builder.append(")");
        try (PreparedStatement st = dbc.prepareStatement(builder.toString())) {
            
            for (int i = 1; i <= numColumns; i++) {
                ColumnValue columnValue = action.getColumn().get(i-1);
                JDBCType jdbcType = JDBCType.valueOf(columnValue.getType());
                Object value = convertValue(columnValue);
                if (value == null) {
                    st.setNull(i, jdbcType.getVendorTypeNumber());
                }
                st.setObject(i, value);
            }
            consumer.accept(st);
        }
        catch (SQLException exc) {
            throw Exceptions.unchecked(exc);
        }
        return VisitorAction.CONTINUE;
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