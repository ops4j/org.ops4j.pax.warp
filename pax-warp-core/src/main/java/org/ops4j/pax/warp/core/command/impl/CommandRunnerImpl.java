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
package org.ops4j.pax.warp.core.command.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.xml.bind.JAXBException;

import org.ops4j.pax.warp.core.changelog.DatabaseChangeLogWriter;
import org.ops4j.pax.warp.core.command.CommandRunner;
import org.ops4j.pax.warp.core.dump.DumpDataService;
import org.ops4j.pax.warp.core.jdbc.DatabaseModel;
import org.ops4j.pax.warp.core.jdbc.DatabaseModelBuilder;
import org.ops4j.pax.warp.core.update.UpdateService;
import org.ops4j.pax.warp.jaxb.gen.ChangeLog;
import org.ops4j.pax.warp.jaxb.gen.ChangeSet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Harald Wellmann
 *
 */
@Component
@Dependent
public class CommandRunnerImpl implements CommandRunner {

    @Inject
    private DumpDataService dumpDataService;

    @Inject
    private UpdateService updateService;

    @Inject
    private DatabaseChangeLogWriter changeLogWriter;

    @Override
    public void dump(String jdbcUrl, String username, String password, OutputStream os)
        throws SQLException, JAXBException {
        try (Connection dbc = DriverManager.getConnection(jdbcUrl, username, password)) {
            dump(dbc, os);
        }
    }

    @Override
    public void dump(DataSource ds, OutputStream os) throws SQLException, JAXBException {
        try (Connection dbc = ds.getConnection()) {
            dump(dbc, os);
        }
    }

    @Override
    public void dump(Connection dbc, OutputStream os) throws SQLException, JAXBException {
        DatabaseModelBuilder inspector = new DatabaseModelBuilder(dbc, null, null);
        DatabaseModel database = inspector.buildDatabaseModel();

        ChangeLog changeLog = new ChangeLog();
        changeLog.setVersion("0.1");
        changeLog.getChangeSetOrInclude();
        database.getTables().forEach(t -> addChangeSet(changeLog, t));
        database.getPrimaryKeys().forEach(t -> addChangeSet(changeLog, t));
        database.getForeignKeys().forEach(t -> addChangeSet(changeLog, t));
        database.getIndexes().forEach(t -> addChangeSet(changeLog, t));

        writeChangeLog(changeLog, os);

    }

    private void addChangeSet(ChangeLog changeLog, Object action) {
        ChangeSet changeSet = new ChangeSet();
        changeSet.setId(UUID.randomUUID().toString());
        List<Object> changes = changeSet.getChanges();
        changes.add(action);
        changeLog.getChangeSetOrInclude().add(changeSet);
    }

    @Override
    public void dumpData(String jdbcUrl, String username, String password, OutputStream os)
        throws SQLException, JAXBException {
        try (Connection dbc = DriverManager.getConnection(jdbcUrl, username, password)) {
            dumpData(dbc, os);
        }
    }

    @Override
    public void dumpData(Connection dbc, OutputStream os) throws SQLException, JAXBException {
        dumpDataService.dumpData(dbc, os);
    }

    private void writeChangeLog(ChangeLog changeLog, OutputStream os) throws JAXBException {
        OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
        changeLogWriter.write(changeLog, writer);
    }

    @Override
    public void update(String jdbcUrl, String username, String password, InputStream is)
        throws JAXBException, SQLException {
        try (Connection dbc = DriverManager.getConnection(jdbcUrl, username, password)) {
            String dbms = getDbms(jdbcUrl);
            update(dbc, is, dbms);
        }
    }

    @Override
    public void update(DataSource ds, InputStream is, String dbms) throws JAXBException,
        SQLException {
        try (Connection dbc = ds.getConnection()) {
            dbc.setAutoCommit(false);
            update(dbc, is, dbms);
        }
    }

    @Override
    public void update(Connection dbc, InputStream is, String dbms) throws JAXBException,
        SQLException {
        updateService.update(dbc, is, dbms);
    }

    /**
     * @param jdbcUrl
     * @return
     */
    private String getDbms(String jdbcUrl) {
        String[] words = jdbcUrl.split(":", 3);
        return words[1];
    }

    /**
     * @param dumpDataService
     *            the dumpDataService to set
     */
    @Reference
    public void setDumpDataService(DumpDataService dumpDataService) {
        this.dumpDataService = dumpDataService;
    }

    /**
     * @param changeLogWriter
     *            the changeLogWriter to set
     */
    @Reference
    public void setChangeLogWriter(DatabaseChangeLogWriter changeLogWriter) {
        this.changeLogWriter = changeLogWriter;
    }

    /**
     * @param updateService
     *            the updateService to set
     */
    @Reference
    public void setUpdateService(UpdateService updateService) {
        this.updateService = updateService;
    }
}
