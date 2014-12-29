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
package org.ops4j.pax.warp.core.insert.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.ops4j.pax.warp.core.changelog.DatabaseChangeLogWriter;
import org.ops4j.pax.warp.core.insert.InsertDataService;
import org.ops4j.pax.warp.core.jdbc.DatabaseModel;
import org.ops4j.pax.warp.core.jdbc.DatabaseModelBuilder;
import org.ops4j.pax.warp.core.update.UpdateService;
import org.ops4j.pax.warp.jaxb.gen.AddForeignKey;
import org.ops4j.pax.warp.jaxb.gen.ChangeLog;
import org.ops4j.pax.warp.jaxb.gen.ChangeSet;
import org.ops4j.pax.warp.jaxb.gen.CreateTable;
import org.ops4j.pax.warp.jaxb.gen.DropForeignKey;
import org.ops4j.pax.warp.jaxb.gen.TruncateTable;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Harald Wellmann
 *
 */
@Dependent
public class InsertDataServiceImpl implements InsertDataService {

    @Inject
    private DatabaseChangeLogWriter changeLogWriter;

    @Inject
    private UpdateService updateService;

    @Override
    public void insertData(Connection dbc, InputStream is, String dbms) throws JAXBException, IOException, SQLException {
        DatabaseModelBuilder inspector = new DatabaseModelBuilder(dbc, null, null);
        DatabaseModel database = inspector.buildDatabaseModel();

        ChangeLog changeLog = new ChangeLog();
        changeLog.setVersion("0.1");
        changeLog.getChangeSetOrInclude();

        ChangeSet changeSet = new ChangeSet();
        changeSet.setId(UUID.randomUUID().toString());
        changeLog.getChangeSetOrInclude().add(changeSet);
        List<Object> changes = changeSet.getChanges();
        dropForeignKeys(changes, database);
        truncateTables(changes, database);

        File preInsertFile = File.createTempFile("warp", ".xml");
        writeChangeLog(changeLog, preInsertFile);

        ChangeLog postChangeLog = new ChangeLog();
        postChangeLog.setVersion("0.1");
        postChangeLog.getChangeSetOrInclude();
        ChangeSet postChangeSet = new ChangeSet();
        postChangeSet.setId(UUID.randomUUID().toString());
        postChangeLog.getChangeSetOrInclude().add(postChangeSet);
        List<Object> postChanges = postChangeSet.getChanges();
        postChanges.addAll(database.getForeignKeys());

        File postInsertFile = File.createTempFile("warp", ".xml");
        writeChangeLog(postChangeLog, postInsertFile);

        try (InputStream preIs = new FileInputStream(preInsertFile)) {
            updateService.update(dbc, preIs, dbms);
        }
        updateService.update(dbc, is, dbms);

        try (InputStream postIs = new FileInputStream(postInsertFile)) {
            updateService.update(dbc, postIs, dbms);
        }
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

    private void writeChangeLog(ChangeLog changeLog, File outputFile) throws JAXBException, IOException {
        try (OutputStream os = new FileOutputStream(outputFile)) {
            OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            changeLogWriter.write(changeLog, writer);
        }
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
