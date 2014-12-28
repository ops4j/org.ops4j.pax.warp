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
package org.ops4j.pax.warp.core.update.impl;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.ops4j.pax.warp.core.changelog.impl.AbstractSqlGenerator;
import org.ops4j.pax.warp.core.history.ChangeLogHistory;
import org.ops4j.pax.warp.core.util.Exceptions;
import org.ops4j.pax.warp.jaxb.WarpJaxbContext;
import org.ops4j.pax.warp.jaxb.gen.AddForeignKey;
import org.ops4j.pax.warp.jaxb.gen.AddPrimaryKey;
import org.ops4j.pax.warp.jaxb.gen.ChangeSet;
import org.ops4j.pax.warp.jaxb.gen.Column;
import org.ops4j.pax.warp.jaxb.gen.ColumnValue;
import org.ops4j.pax.warp.jaxb.gen.CreateTable;
import org.ops4j.pax.warp.jaxb.gen.DropForeignKey;
import org.ops4j.pax.warp.jaxb.gen.DropPrimaryKey;
import org.ops4j.pax.warp.jaxb.gen.Insert;
import org.ops4j.pax.warp.jaxb.gen.TruncateTable;
import org.ops4j.pax.warp.jaxb.gen.visitor.VisitorAction;

public class UpdateSqlGenerator extends AbstractSqlGenerator {

    private WarpJaxbContext context;
    private String actualChecksum;
    private ChangeLogHistory changeLogHistory;
    private Set<String> autoIncrementColumns;

    public UpdateSqlGenerator(String dbms, Connection dbc, Consumer<PreparedStatement> consumer,
        WarpJaxbContext context) {
        super(dbms, dbc, consumer);
        this.context = context;
        this.autoIncrementColumns = new HashSet<>();
    }

    @Override
    public VisitorAction enter(CreateTable action) {
        if ("mysql".equals(dbms)) {
            action.getColumn().stream().filter(c -> isAutoIncrement(c))
                .forEach(c -> autoIncrementColumns.add(action.getTableName() + "." + c.getName()));
        }
        return produceStatement("createTable", action);
    }

    private boolean isAutoIncrement(Column c) {
        if (c.isAutoIncrement() == null) {
            return false;
        }
        return c.isAutoIncrement();
    }

    @Override
    public VisitorAction enter(AddPrimaryKey action) {
        if ("mysql".equals(dbms)) {
            String columnKey = action.getTableName() + "." + action.getColumn().get(0);
            if (autoIncrementColumns.contains(columnKey)) {
                return VisitorAction.SKIP;
            }
        }
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
    public VisitorAction enter(ChangeSet changeSet) {
        actualChecksum = computeChecksum(changeSet);
        String id = changeSet.getId();
        String expectedChecksum = changeLogHistory.get(id);
        if (expectedChecksum != null && !expectedChecksum.equals(actualChecksum)) {
            String msg = String.format("checksum mismatch for change set [id=%s]", id);
            throw new IllegalArgumentException(msg);
        }
        return VisitorAction.CONTINUE;
    }

    @Override
    public VisitorAction leave(ChangeSet changeSet) {
        try {
            PreparedStatement st = dbc
                .prepareStatement("insert into warp_history (id, checksum, executed) values (?, ?, ?)");
            setValues(st, changeSet.getId(), actualChecksum,
                new Timestamp(System.currentTimeMillis()));
            st.executeUpdate();
            st.close();
            dbc.commit();
        }
        catch (SQLException exc) {
            throw Exceptions.unchecked(exc);
        }
        return VisitorAction.CONTINUE;
    }

    public static void setValues(PreparedStatement preparedStatement, Object... values)
        throws SQLException {
        for (int i = 0; i < values.length; i++) {
            preparedStatement.setObject(i + 1, values[i]);
        }
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

    public String computeChecksum(ChangeSet changeSet) {
        try {
            Marshaller marshaller = context.createFragmentMarshaller();
            StringWriter writer = new StringWriter();
            marshaller.marshal(changeSet, writer);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(writer.toString().getBytes(StandardCharsets.UTF_8));
            byte[] checksum = digest.digest();
            return new BigInteger(1, checksum).toString(16);
        }
        catch (JAXBException | NoSuchAlgorithmException exc) {
            throw Exceptions.unchecked(exc);
        }
    }

    public void setChangeLogHistory(ChangeLogHistory history) {
        this.changeLogHistory = history;
    }
}
