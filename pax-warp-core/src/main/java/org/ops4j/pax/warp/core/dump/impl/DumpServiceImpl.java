/*
 * Copyright 2014 Harald Wellmann.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 * 
 * See the License for the specific language governing permissions and limitations under the
 * License.
 */
package org.ops4j.pax.warp.core.dump.impl;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.ops4j.pax.warp.core.changelog.ChangeLogWriter;
import org.ops4j.pax.warp.core.dump.DumpService;
import org.ops4j.pax.warp.core.jdbc.DatabaseModel;
import org.ops4j.pax.warp.core.jdbc.DatabaseModelBuilder;
import org.ops4j.pax.warp.exc.WarpException;
import org.ops4j.pax.warp.jaxb.gen.ChangeLog;
import org.ops4j.pax.warp.jaxb.gen.ChangeSet;
import org.ops4j.pax.warp.jaxb.gen.Column;
import org.ops4j.pax.warp.jaxb.gen.ColumnValue;
import org.ops4j.pax.warp.jaxb.gen.CreateTable;
import org.ops4j.pax.warp.jaxb.gen.Insert;
import org.ops4j.pax.warp.scope.CdiDependent;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements {@link DumpService}.
 *
 * @author Harald Wellmann
 *
 */
@Component
@CdiDependent
@Named
public class DumpServiceImpl implements DumpService {

    private static Logger log = LoggerFactory.getLogger(DumpServiceImpl.class);

    @Inject
    private ChangeLogWriter changeLogWriter;

    @Override
    public void dumpStructure(Connection dbc, OutputStream os) {
        DatabaseModelBuilder inspector = new DatabaseModelBuilder(dbc);
        DatabaseModel database = inspector.buildDatabaseModel();

        ChangeLog changeLog = new ChangeLog();
        changeLog.setVersion("0.1");
        changeLog.getChangeSet();
        database.getTables().forEach(t -> addChangeSet(changeLog, t));
        database.getPrimaryKeys().forEach(t -> addChangeSet(changeLog, t));
        database.getForeignKeys().forEach(t -> addChangeSet(changeLog, t));
        database.getIndexes().forEach(t -> addChangeSet(changeLog, t));

        changeLogWriter.writeChangeLog(changeLog, os);

    }

    private void addChangeSet(ChangeLog changeLog, Object action) {
        ChangeSet changeSet = new ChangeSet();
        changeSet.setId(UUID.randomUUID().toString());
        List<Object> changes = changeSet.getChanges();
        changes.add(action);
        assert !changeSet.getChanges().isEmpty();
        changeLog.getChangeSet().add(changeSet);
    }

    @Override
    public void dumpData(Connection dbc, OutputStream os) {
        DatabaseModelBuilder inspector = new DatabaseModelBuilder(dbc);
        DatabaseModel database = inspector.buildDatabaseModel();

        ChangeLog changeLog = new ChangeLog();
        changeLog.setVersion("0.1");

        insertData(changeLog, database, dbc);

        changeLogWriter.writeChangeLog(changeLog, os);
    }

    private void insertData(ChangeLog changeLog, DatabaseModel database, Connection dbc) {
        ChangeSet changeSet = new ChangeSet();
        changeSet.setId(UUID.randomUUID().toString());
        changeLog.getChangeSet().add(changeSet);
        List<Object> changes = changeSet.getChanges();
        for (CreateTable createTable : database.getTables()) {
            insertData(changes, createTable, dbc);
        }
    }

    private void insertData(List<Object> changes, CreateTable createTable, Connection dbc) {
        log.info("selecting data from {}", createTable.getTableName());
        String columns = createTable.getColumn().stream().map(c -> c.getName())
            .collect(Collectors.joining(", "));
        String sql = String.format("select %s from %s", columns, createTable.getTableName());
        try (Statement st = dbc.createStatement(); //
            ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData metaData = rs.getMetaData();
            while (rs.next()) {
                Insert insert = new Insert();
                insert.setCatalogName(createTable.getCatalogName());
                insert.setSchemaName(createTable.getSchemaName());
                insert.setTableName(createTable.getTableName());
                List<ColumnValue> columnValues = insert.getColumn();

                readColumnValues(createTable, rs, metaData, columnValues);
                changes.add(insert);
            }
            dbc.setAutoCommit(false);
            dbc.commit();
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }
    }

    private void readColumnValues(CreateTable createTable, ResultSet rs,
        ResultSetMetaData metaData, List<ColumnValue> columnValues) throws SQLException {
        int col = 1;
        for (Column column : createTable.getColumn()) {
            ColumnValue columnValue = readColumnValue(rs, metaData, col, column);
            columnValues.add(columnValue);
            col++;
        }
    }

    /**
     * Reads a columns value from the result set and converts it to a string.
     * <p>
     * NOTE: {@code rs.getObject(col)} should be sufficient in theory, but at least for H2 CLOB
     * columns, it returns strings like {@code clob0: 'Foobar'}, so we switch on the type and use
     * {@code rs.getString(col)} where appropriate.
     * 
     * @param rs
     *            result set
     * @param metaData
     *            metadata for the given table
     * @param col
     *            column index
     * @param column
     *            column descriptor
     * @return column value
     * @throws SQLException
     */
    private ColumnValue readColumnValue(ResultSet rs, ResultSetMetaData metaData, int col,
        Column column) throws SQLException {

        Object value = null;
        JDBCType jdbcType = JDBCType.valueOf(metaData.getColumnType(col));
        switch (jdbcType) {
            case CLOB:
            case CHAR:
            case VARCHAR:
                value = rs.getString(col);
                break;
                
            case BLOB:
            case BINARY:
            case VARBINARY:
            case LONGVARBINARY:
                byte[] bytes = rs.getBytes(col);
                if (bytes != null) {
                    value = Base64.getEncoder().encodeToString(bytes);
                }
                break;
                
            default:
                value = rs.getObject(col);
        }

        ColumnValue columnValue = new ColumnValue();
        columnValue.setName(column.getName());
        columnValue.setType(jdbcType.toString());
        if (rs.wasNull()) {
            columnValue.setNull(true);
        }
        else {
            columnValue.setValue(value.toString());
        }
        return columnValue;
    }

    /**
     * Sets the change log writer.
     *
     * @param changeLogWriter
     *            change log writer
     */
    @Reference
    public void setChangeLogWriter(ChangeLogWriter changeLogWriter) {
        this.changeLogWriter = changeLogWriter;
    }
}
