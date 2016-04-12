/*
 * Copyright 2016 Harald Wellmann.
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
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.function.Consumer;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.ops4j.pax.warp.core.changelog.impl.BaseSqlGenerator;
import org.ops4j.pax.warp.core.dbms.DbmsProfile;
import org.ops4j.pax.warp.exc.WarpException;
import org.ops4j.pax.warp.jaxb.WarpJaxbContext;
import org.ops4j.pax.warp.jaxb.gen.ChangeSet;
import org.ops4j.pax.warp.jaxb.gen.ColumnValue;
import org.ops4j.pax.warp.jaxb.gen.Insert;
import org.ops4j.pax.warp.jaxb.gen.ObjectFactory;
import org.ops4j.pax.warp.jaxb.gen.visitor.VisitorAction;

public class InsertSqlGenerator extends BaseSqlGenerator {

    private WarpJaxbContext context;

    public InsertSqlGenerator(DbmsProfile dbms, Connection dbc, Consumer<PreparedStatement> consumer,
        WarpJaxbContext context) {
        super(dbms, dbc, consumer);
        this.context = context;
    }

    protected VisitorAction generateInsert(Insert action) {
        if (dbms.getTableNameIsCaseSensitive()) {
            action.setTableName(action.getTableName().toLowerCase());
        }
        String rawSql = engine.renderTemplate("insert", action);
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
            throw new WarpException(exc);
        }
        return VisitorAction.SKIP;
    }

    /**
     * @param columnValue
     * @return
     */
    private Object convertValue(ColumnValue columnValue) {
        if (columnValue.isNull()) {
            return null;
        }
        JDBCType jdbcType = JDBCType.valueOf(columnValue.getType());
        String value = columnValue.getValue();
        switch (jdbcType) {
            case BIGINT:
                return Long.parseLong(value);
            case BIT:
            case BOOLEAN:
                return Boolean.parseBoolean(value);
            case CHAR:
            case CLOB:
            case VARCHAR:
            case LONGVARCHAR:
                return value;
            case DATE:
                return Date.valueOf(value);
            case DECIMAL:
            case NUMERIC:
                return new BigDecimal(value);
            case DOUBLE:
                return Double.parseDouble(value);
            case INTEGER:
                return Integer.parseInt(value);
            case BINARY:
            case BLOB:
            case LONGVARBINARY:
            case VARBINARY:
                return Base64.getDecoder().decode(value);
            case NULL:
                return null;
            case SMALLINT:
                return Short.parseShort(value);
            case TIME:
                return Time.valueOf(value);
            case TINYINT:
                return Byte.parseByte(value);
            case TIMESTAMP:
            case TIMESTAMP_WITH_TIMEZONE:
                return Timestamp.valueOf(value);
            default:
                throw new IllegalArgumentException("cannot convert JDBCType " + jdbcType.toString());
        }
    }

    public String computeChecksum(ChangeSet changeSet) {
        try {
            Marshaller marshaller = context.createFragmentMarshaller();
            StringWriter writer = new StringWriter();
            marshaller.marshal(new ObjectFactory().createChangeSet(changeSet), writer);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(writer.toString().getBytes(StandardCharsets.UTF_8));
            byte[] checksum = digest.digest();
            return new BigInteger(1, checksum).toString(16);
        }
        catch (JAXBException | NoSuchAlgorithmException exc) {
            throw new WarpException(exc);
        }
    }
}
