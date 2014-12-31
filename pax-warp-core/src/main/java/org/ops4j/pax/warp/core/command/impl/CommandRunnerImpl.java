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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.ops4j.pax.warp.core.changelog.ChangeLogWriter;
import org.ops4j.pax.warp.core.command.CommandRunner;
import org.ops4j.pax.warp.core.dump.DumpDataService;
import org.ops4j.pax.warp.core.jdbc.DatabaseModel;
import org.ops4j.pax.warp.core.jdbc.DatabaseModelBuilder;
import org.ops4j.pax.warp.core.update.UpdateService;
import org.ops4j.pax.warp.exc.WarpException;
import org.ops4j.pax.warp.jaxb.gen.ChangeLog;
import org.ops4j.pax.warp.jaxb.gen.ChangeSet;
import org.ops4j.pax.warp.scope.CdiDependent;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Harald Wellmann
 *
 */
@Component
@CdiDependent
@Named
public class CommandRunnerImpl implements CommandRunner {

    @Inject
    private DumpDataService dumpDataService;

    @Inject
    private UpdateService updateService;

    @Inject
    private ChangeLogWriter changeLogWriter;

    @Override
    public void dump(String jdbcUrl, String username, String password, OutputStream os) {
        try (Connection dbc = DriverManager.getConnection(jdbcUrl, username, password)) {
            dump(dbc, os);
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }
    }

    @Override
    public void dump(DataSource ds, OutputStream os) {
        try (Connection dbc = ds.getConnection()) {
            dump(dbc, os);
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }
    }

    @Override
    public void dump(Connection dbc, OutputStream os) {
        DatabaseModelBuilder inspector = new DatabaseModelBuilder(dbc);
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
        assert !changeSet.getChanges().isEmpty();
        changeLog.getChangeSetOrInclude().add(changeSet);
    }

    @Override
    public void dumpData(String jdbcUrl, String username, String password, OutputStream os) {
        try (Connection dbc = DriverManager.getConnection(jdbcUrl, username, password)) {
            dumpData(dbc, os);
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }
    }

    @Override
    public void dumpDataOnly(Connection dbc, OutputStream os) {
        dumpDataService.dumpDataOnly(dbc, os);
    }

    @Override
    public void dumpDataOnly(String jdbcUrl, String username, String password, OutputStream os) {
        try (Connection dbc = DriverManager.getConnection(jdbcUrl, username, password)) {
            dumpDataOnly(dbc, os);
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }
    }

    @Override
    public void dumpData(Connection dbc, OutputStream os) {
        dumpDataService.dumpData(dbc, os);
    }

    private void writeChangeLog(ChangeLog changeLog, OutputStream os) {
        OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
        changeLogWriter.write(changeLog, writer);
    }

    @Override
    public void migrate(String jdbcUrl, String username, String password, InputStream is) {
        try (Connection dbc = DriverManager.getConnection(jdbcUrl, username, password)) {
            String dbms = getDbms(jdbcUrl);
            migrate(dbc, is, dbms);
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }
    }

    @Override
    public void migrate(DataSource ds, InputStream is, String dbms)  {
        try (Connection dbc = ds.getConnection()) {
            dbc.setAutoCommit(false);
            migrate(dbc, is, dbms);
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }
    }

    @Override
    public void migrate(Connection dbc, InputStream is, String dbms)  {
        updateService.migrate(dbc, is, dbms);
    }

    @Override
    public void insertData(Connection dbc, InputStream is, String dbms) {
        updateService.importData(dbc, is, dbms, Collections.emptyList());
    }

    @Override
    public void insertData(Connection dbc, InputStream is, String dbms, List<String> excludedTables) {
        updateService.importData(dbc, is, dbms, excludedTables);
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
    public void setChangeLogWriter(ChangeLogWriter changeLogWriter) {
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
