/*
 * Copyright 2014 Harald Wellmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.ops4j.pax.warp.core.command.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.ops4j.pax.warp.core.command.CommandRunner;
import org.ops4j.pax.warp.core.dbms.DbmsProfile;
import org.ops4j.pax.warp.core.dbms.DbmsProfileSelector;
import org.ops4j.pax.warp.core.dump.DumpService;
import org.ops4j.pax.warp.core.update.UpdateService;
import org.ops4j.pax.warp.exc.WarpException;
import org.ops4j.pax.warp.jaxb.gen.ChangeLog;
import org.ops4j.pax.warp.scope.CdiDependent;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Implements {@link CommandRunner}.
 *
 * @author Harald Wellmann
 *
 */
@Component
@CdiDependent
@Named
public class CommandRunnerImpl implements CommandRunner {

    @Inject
    private DumpService dumpDataService;

    @Inject
    private UpdateService updateService;

    @Inject
    private DbmsProfileSelector profileSelector;

    @Override
    public void dumpStructure(String jdbcUrl, String username, String password, OutputStream os) {
        try (Connection dbc = DriverManager.getConnection(jdbcUrl, username, password)) {
            dumpStructure(dbc, os);
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }
    }

    @Override
    public void dumpStructure(DataSource ds, OutputStream os) {
        try (Connection dbc = ds.getConnection()) {
            dumpStructure(dbc, os);
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }
    }

    @Override
    public void dumpStructure(Connection dbc, OutputStream os) {
        dumpDataService.dumpStructure(dbc, os, getDbms(dbc), Optional.empty());
    }

    @Override
    public void dumpStructure(Connection dbc, OutputStream os, String schema) {
        dumpDataService.dumpStructure(dbc, os, getDbms(dbc), Optional.empty());
    }

    @Override
    public void dumpData(Connection dbc, OutputStream os) {
        dumpDataService.dumpData(dbc, os, getDbms(dbc), Optional.empty());
    }

    @Override
    public void dumpAll(Connection dbc, OutputStream os) {
        dumpDataService.dumpAll(dbc, os, getDbms(dbc), Optional.empty());
    }

    @Override
    public void dumpData(Connection dbc, OutputStream os, String schema) {
        dumpDataService.dumpData(dbc, os, getDbms(dbc), Optional.of(schema));
    }

    @Override
    public void dumpData(DataSource ds, OutputStream os) {
        try (Connection dbc = ds.getConnection()) {
            dumpDataService.dumpData(dbc, os, getDbms(dbc), Optional.empty());
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }
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
    public void dumpAll(String jdbcUrl, String username, String password, OutputStream os) {
        try (Connection dbc = DriverManager.getConnection(jdbcUrl, username, password)) {
            dumpAll(dbc, os);
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }
    }

    @Override
    public void migrate(String jdbcUrl, String username, String password, InputStream is) {
        try (Connection dbc = DriverManager.getConnection(jdbcUrl, username, password)) {
            migrate(dbc, is);
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }
    }

    @Override
    public void migrate(DataSource ds, InputStream is) {
        try (Connection dbc = ds.getConnection()) {
            dbc.setAutoCommit(false);
            migrate(dbc, is);
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }
    }

    @Override
    public void migrate(Connection dbc, InputStream is) {
        updateService.migrate(dbc, is, getDbms(dbc), Optional.empty());
    }

    @Override
    public void migrate(Connection dbc, ChangeLog changeLog) {
        updateService.migrate(dbc, changeLog, getDbms(dbc), Optional.empty());
    }

    @Override
    public void migrate(Connection dbc, ChangeLog changeLog, String schema) {
        updateService.migrate(dbc, changeLog, getDbms(dbc), Optional.of(schema));
    }

    @Override
    public void migrate(Connection dbc, InputStream is, String schema) {
        updateService.migrate(dbc, is, getDbms(dbc), Optional.of(schema));
    }

    @Override
    public void importData(String jdbcUrl, String username, String password, InputStream is,
        List<String> excludedTables) {
        try (Connection dbc = DriverManager.getConnection(jdbcUrl, username, password)) {
            importData(dbc, is);
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }
    }

    @Override
    public void importData(String jdbcUrl, String username, String password, InputStream is) {
        importData(jdbcUrl, username, password, is, Collections.emptyList());
    }

    @Override
    public void importData(Connection dbc, InputStream is) {
        importData(dbc, is, Collections.emptyList());
    }

    @Override
    public void importData(Connection dbc, InputStream is, List<String> excludedTables) {
        updateService.importData(dbc, is, getDbms(dbc), Optional.empty(), excludedTables);
    }

    @Override
    public void importData(Connection dbc, InputStream is, String schema) {
        updateService.importData(dbc, is, getDbms(dbc), Optional.of(schema),
            Collections.emptyList());
    }

    @Override
    public void importData(DataSource ds, InputStream is) {
        importData(ds, is, Collections.emptyList());
    }

    @Override
    public void importData(DataSource ds, InputStream is, List<String> excludedTables) {
        try (Connection dbc = ds.getConnection()) {
            dbc.setAutoCommit(false);
            updateService.importData(dbc, is, getDbms(dbc), Optional.empty(), excludedTables);
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }
    }

    private DbmsProfile getDbms(Connection dbc) {
        return profileSelector.selectProfile(dbc);
    }

    /**
     * @param dumpDataService
     *            the dumpDataService to set
     */
    @Reference
    public void setDumpDataService(DumpService dumpDataService) {
        this.dumpDataService = dumpDataService;
    }

    /**
     * @param updateService
     *            the updateService to set
     */
    @Reference
    public void setUpdateService(UpdateService updateService) {
        this.updateService = updateService;
    }

    /**
     * @param profileSelector
     *            the profileSelector to set
     */
    @Reference
    public void setProfileSelector(DbmsProfileSelector profileSelector) {
        this.profileSelector = profileSelector;
    }
}
