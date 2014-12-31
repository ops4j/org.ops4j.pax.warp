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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBException;

import org.ops4j.pax.warp.core.changelog.ChangeLogReader;
import org.ops4j.pax.warp.core.changelog.impl.ChangeLogService;
import org.ops4j.pax.warp.core.history.ChangeLogHistory;
import org.ops4j.pax.warp.core.history.ChangeLogHistoryService;
import org.ops4j.pax.warp.core.jdbc.DatabaseModel;
import org.ops4j.pax.warp.core.jdbc.DatabaseModelBuilder;
import org.ops4j.pax.warp.core.update.UpdateService;
import org.ops4j.pax.warp.exc.WarpException;
import org.ops4j.pax.warp.jaxb.WarpJaxbContext;
import org.ops4j.pax.warp.jaxb.gen.ChangeLog;
import org.ops4j.pax.warp.jaxb.gen.ChangeSet;
import org.ops4j.pax.warp.jaxb.gen.CreateTable;
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
public class UpdateServiceImpl implements UpdateService {

    @Inject
    private ChangeLogReader changeLogReader;

    @Inject
    private ChangeLogHistoryService historyService;

    @Inject
    private ChangeLogService changeLogService;

    @Inject
    private WarpJaxbContext context;

    @Override
    public void migrate(Connection dbc, InputStream is, String dbms) {
        boolean autoCommit = false;
        try {
            autoCommit = dbc.getAutoCommit();
            dbc.setAutoCommit(false);
            UpdateSqlGenerator generator = new UpdateSqlGenerator(dbms, dbc, s -> runUpdate(s), context);
            if (!historyService.hasMetaDataTable(dbc)) {
                CreateTable action = historyService.createHistoryTableAction();
                action.accept(generator);
            }

            ChangeLogHistory history = historyService.readChangeLogHistory(dbc);
            generator.setChangeLogHistory(history);
            generator.setChangeSetFilter(c -> !history.containsKey(c.getId()));

            ChangeLog changeLog = readChangeLog(is);
            changeLog.accept(generator);
            dbc.setAutoCommit(autoCommit);
        }
        catch (SQLException | JAXBException exc) {
            throw new WarpException(exc);
        }
    }

    @Override
    public void importData(Connection dbc, InputStream is, String dbms, List<String> excludedTables) {
        DatabaseModelBuilder inspector = new DatabaseModelBuilder(dbc);
        DatabaseModel database = inspector.buildDatabaseModel();
        excludedTables.forEach(t -> database.removeTable(t));

        ChangeLog changeLog = new ChangeLog();
        changeLog.setVersion("0.1");
        changeLog.getChangeSet();

        ChangeSet changeSet = new ChangeSet();
        changeSet.setId(UUID.randomUUID().toString());
        changeLog.getChangeSet().add(changeSet);
        List<Object> changes = changeSet.getChanges();
        changeLogService.dropForeignKeys(changes, database);
        changeLogService.truncateTables(changes, database);

        File preInsertFile = createTempFile();
        changeLogService.writeChangeLog(changeLog, preInsertFile);

        ChangeLog postChangeLog = new ChangeLog();
        postChangeLog.setVersion("0.1");
        postChangeLog.getChangeSet();
        ChangeSet postChangeSet = new ChangeSet();
        postChangeSet.setId(UUID.randomUUID().toString());
        postChangeLog.getChangeSet().add(postChangeSet);
        List<Object> postChanges = postChangeSet.getChanges();
        postChanges.addAll(database.getForeignKeys());

        File postInsertFile = createTempFile();
        changeLogService.writeChangeLog(postChangeLog, postInsertFile);

        update(dbc, preInsertFile, dbms);
        migrate(dbc, is, dbms);
        update(dbc, postInsertFile, dbms);
    }

    private File createTempFile() {
        try {
            return File.createTempFile("warp", ".xml");
        }
        catch (IOException exc) {
            throw new WarpException(exc);
        }
    }

    private void update(Connection dbc, File changeLogFile, String dbms) {
        try (InputStream preIs = new FileInputStream(changeLogFile)) {
            migrate(dbc, preIs, dbms);
        }
        catch (IOException exc) {
            throw new WarpException(exc);
        }
    }

    private void runUpdate(PreparedStatement st) {
        try {
            st.executeUpdate();
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }
    }

    private ChangeLog readChangeLog(InputStream is) throws JAXBException {
        InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
        return changeLogReader.parse(reader);
    }


    /**
     * @param changeLogReader the changeLogReader to set
     */
    @Reference
    public void setChangeLogReader(ChangeLogReader changeLogReader) {
        this.changeLogReader = changeLogReader;
    }


    /**
     * @param historyService the historyService to set
     */
    @Reference
    public void setHistoryService(ChangeLogHistoryService historyService) {
        this.historyService = historyService;
    }


    /**
     * @param context the context to set
     */
    @Reference
    public void setContext(WarpJaxbContext context) {
        this.context = context;
    }
}
