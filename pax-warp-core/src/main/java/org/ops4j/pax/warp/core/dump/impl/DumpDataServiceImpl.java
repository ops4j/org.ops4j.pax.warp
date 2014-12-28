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
package org.ops4j.pax.warp.core.dump.impl;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.ops4j.pax.warp.core.changelog.DatabaseChangeLogWriter;
import org.ops4j.pax.warp.core.dump.DumpDataService;
import org.ops4j.pax.warp.core.jdbc.DatabaseModel;
import org.ops4j.pax.warp.core.jdbc.DatabaseModelBuilder;
import org.ops4j.pax.warp.core.util.Exceptions;
import org.ops4j.pax.warp.jaxb.gen.AddForeignKey;
import org.ops4j.pax.warp.jaxb.gen.ChangeLog;
import org.ops4j.pax.warp.jaxb.gen.ChangeSet;
import org.ops4j.pax.warp.jaxb.gen.Column;
import org.ops4j.pax.warp.jaxb.gen.ColumnValue;
import org.ops4j.pax.warp.jaxb.gen.CreateTable;
import org.ops4j.pax.warp.jaxb.gen.DropForeignKey;
import org.ops4j.pax.warp.jaxb.gen.Insert;
import org.ops4j.pax.warp.jaxb.gen.TruncateTable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Harald Wellmann
 *
 */
@Dependent
@Component
public class DumpDataServiceImpl implements DumpDataService {

    @Inject
    private DatabaseChangeLogWriter changeLogWriter;

    @Override
    public void dumpData(Connection dbc, OutputStream os) throws JAXBException {
        DatabaseModelBuilder inspector = new DatabaseModelBuilder(dbc, null, null);
        DatabaseModel database = inspector.buildDatabaseModel();

        ChangeLog changeLog = new ChangeLog();
        changeLog.setVersion("0.1");
        changeLog.getChangeSetOrInclude();

        ChangeSet changeSet = new ChangeSet();
        changeSet.setId("1");
        changeLog.getChangeSetOrInclude().add(changeSet);
        List<Object> changes = changeSet.getChanges();
        dropForeignKeys(changes, database);
        truncateTables(changes, database);
        insertData(changes, database, dbc);
        changes.addAll(database.getForeignKeys());

        writeChangeLog(changeLog, os);
    }

    /**
     * @param changes
     * @param database
     */
    private void dropForeignKeys(List<Object> changes, DatabaseModel database) {
        for (AddForeignKey addFk : database.getForeignKeys()) {
            DropForeignKey dropFk = new DropForeignKey();
            dropFk.setBaseTable(addFk.getBaseTable());
            dropFk.setConstraintName(addFk.getConstraintName());
            changes.add(dropFk);
        }
    }

    private void truncateTables(List<Object> changes, DatabaseModel database) {
        for (CreateTable createTable : database.getTables()) {
            TruncateTable truncateTable = new TruncateTable();
            truncateTable.setCatalogName(createTable.getCatalogName());
            truncateTable.setSchemaName(createTable.getSchemaName());
            truncateTable.setTableName(createTable.getTableName());
            changes.add(truncateTable);
        }
    }

    private void insertData(List<Object> changes, DatabaseModel database, Connection dbc) {
        for (CreateTable createTable : database.getTables()) {
            insertData(changes, createTable, dbc);
        }
    }

    /**
     * @param changes
     * @param createTable
     * @param dbc
     */
    private void insertData(List<Object> changes, CreateTable createTable, Connection dbc) {
        String columns = createTable.getColumn().stream().map(c -> c.getName())
            .collect(Collectors.joining(", "));
        String sql = String.format("select %s from %s", columns, createTable.getTableName());
        try (Statement st = dbc.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData metaData = rs.getMetaData();
            while (rs.next()) {
                Insert insert = new Insert();
                insert.setCatalogName(createTable.getCatalogName());
                insert.setSchemaName(createTable.getSchemaName());
                insert.setTableName(createTable.getTableName());
                List<ColumnValue> columnValues = insert.getColumn();

                int col = 1;
                for (Column column : createTable.getColumn()) {
                    Object value = rs.getObject(col);
                    JDBCType jdbcType = JDBCType.valueOf(metaData.getColumnType(col));
                    ColumnValue columnValue = new ColumnValue();
                    columnValue.setName(column.getName());
                    if (rs.wasNull()) {
                        columnValue.setType("NULL");
                    }
                    else {
                        columnValue.setType(jdbcType.toString());
                        columnValue.setValue(value.toString());
                    }
                    columnValues.add(columnValue);
                    col++;
                }
                changes.add(insert);
            }
        }
        catch (SQLException exc) {
            throw Exceptions.unchecked(exc);
        }
    }

    private void writeChangeLog(ChangeLog changeLog, OutputStream os) throws JAXBException {
        OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
        changeLogWriter.write(changeLog, writer);
    }

    /**
     * @param changeLogWriter
     *            the changeLogWriter to set
     */
    @Reference
    public void setChangeLogWriter(DatabaseChangeLogWriter changeLogWriter) {
        this.changeLogWriter = changeLogWriter;
    }
}
