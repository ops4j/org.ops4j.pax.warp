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
package org.ops4j.pax.warp.command;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;

import org.ops4j.pax.warp.changelog.DatabaseChangeLogWriter;
import org.ops4j.pax.warp.changelog.impl.JaxbDatabaseChangeLogReader;
import org.ops4j.pax.warp.changelog.impl.JaxbDatabaseChangeLogWriter;
import org.ops4j.pax.warp.changelog.impl.UpdateSqlGenerator;
import org.ops4j.pax.warp.dump.DumpDataService;
import org.ops4j.pax.warp.dump.impl.DumpDataServiceImpl;
import org.ops4j.pax.warp.jaxb.ChangeSet;
import org.ops4j.pax.warp.jaxb.DatabaseChangeLog;
import org.ops4j.pax.warp.jdbc.DatabaseModel;
import org.ops4j.pax.warp.jdbc.DatabaseModelBuilder;
import org.ops4j.pax.warp.util.Exceptions;

/**
 * @author Harald Wellmann
 *
 */
public class CommandRunner {

    public void dump(String jdbcUrl, String username, String password, OutputStream os)
        throws SQLException, JAXBException {
        Connection dbc = DriverManager.getConnection(jdbcUrl, username, password);
        dump(dbc, os);
    }

    public void dump(DataSource ds, OutputStream os) throws SQLException, JAXBException {
        try (Connection dbc = ds.getConnection()) {
            dump(dbc, os);
        }
    }

    public void dump(Connection dbc, OutputStream os) throws SQLException, JAXBException {
        DatabaseModelBuilder inspector = new DatabaseModelBuilder(dbc, null, null);
        DatabaseModel database = inspector.buildDatabaseModel();

        DatabaseChangeLog changeLog = new DatabaseChangeLog();
        changeLog.getChangeSetOrInclude();
        database.getTables().forEach(t -> addChangeSet(changeLog, t));
        database.getPrimaryKeys().forEach(t -> addChangeSet(changeLog, t));
        database.getForeignKeys().forEach(t -> addChangeSet(changeLog, t));

        writeChangeLog(changeLog, os);

    }

    private void addChangeSet(DatabaseChangeLog changeLog, Object action) {
        ChangeSet changeSet = new ChangeSet();
        List<Object> changes = changeSet.getCreateTableOrAddPrimaryKeyOrAddForeignKey();
        changes.add(action);
        changeLog.getChangeSetOrInclude().add(changeSet);
    }

    public void dumpData(String jdbcUrl, String username, String password, OutputStream os)
        throws SQLException, JAXBException {
        Connection dbc = DriverManager.getConnection(jdbcUrl, username, password);
        dumpData(dbc, os);
    }

    public void dumpData(Connection dbc, OutputStream os) throws SQLException, JAXBException {
        DumpDataService service = new DumpDataServiceImpl();
        service.dumpData(dbc, os);
    }

    private void writeChangeLog(DatabaseChangeLog changeLog, OutputStream os) throws JAXBException {
        DatabaseChangeLogWriter changeLogWriter = new JaxbDatabaseChangeLogWriter();
        OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
        changeLogWriter.write(changeLog, writer);
    }

    public void update(String jdbcUrl, String username, String password, InputStream is)
        throws JAXBException, SQLException {
        Connection dbc = DriverManager.getConnection(jdbcUrl, username, password);
        String dbms = getDbms(jdbcUrl);
        update(dbc, is, dbms);
    }

    public void update(DataSource ds, InputStream is, String dbms) throws JAXBException,
        SQLException {
        try (Connection dbc = ds.getConnection()) {
            dbc.setAutoCommit(false);
            update(dbc, is, dbms);
        }
    }

    public void update(Connection dbc, InputStream is, String dbms) throws JAXBException,
        SQLException {
        boolean autoCommit = dbc.getAutoCommit();
        try {
            dbc.setAutoCommit(false);
            DatabaseChangeLog changeLog = readChangeLog(is);
            UpdateSqlGenerator generator = new UpdateSqlGenerator(dbms, dbc, s -> runUpdate(s));
            changeLog.accept(generator);
        }
        finally {
            dbc.setAutoCommit(autoCommit);
        }
    }

    /**
     * @param jdbcUrl
     * @return
     */
    private String getDbms(String jdbcUrl) {
        String[] words = jdbcUrl.split(":", 3);
        return words[1];
    }

    private void runUpdate(PreparedStatement st) {
        try {
            st.executeUpdate();
        }
        catch (SQLException exc) {
            throw Exceptions.unchecked(exc);
        }
    }

    private DatabaseChangeLog readChangeLog(InputStream is) throws JAXBException {
        JaxbDatabaseChangeLogReader changeLogReader = new JaxbDatabaseChangeLogReader();
        InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
        DatabaseChangeLog changeLog = changeLogReader.parse(reader);
        return changeLog;
    }
}
