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
package org.ops4j.pax.warp.core.update;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.ops4j.pax.warp.core.changelog.impl.JaxbDatabaseChangeLogReader;
import org.ops4j.pax.warp.core.changelog.impl.UpdateSqlGenerator;
import org.ops4j.pax.warp.core.history.ChangeLogHistory;
import org.ops4j.pax.warp.core.history.ChangeLogHistoryService;
import org.ops4j.pax.warp.core.util.Exceptions;
import org.ops4j.pax.warp.jaxb.ChangeLog;
import org.ops4j.pax.warp.jaxb.CreateTable;
import org.ops4j.pax.warp.jaxb.WarpJaxbContext;


/**
 * @author Harald Wellmann
 *
 */
@Dependent
public class UpdateServiceImpl implements UpdateService {

    @Inject
    private JaxbDatabaseChangeLogReader changeLogReader;

    @Inject
    private ChangeLogHistoryService historyService;

    @Inject
    private WarpJaxbContext context;

    @Override
    public void update(Connection dbc, InputStream is, String dbms) throws SQLException, JAXBException {
        boolean autoCommit = dbc.getAutoCommit();
        try {
            dbc.setAutoCommit(false);
            UpdateSqlGenerator generator = new UpdateSqlGenerator(dbms, dbc, s -> runUpdate(s), context);
            if (!historyService.hasMetaDataTable(dbc)) {
                CreateTable action = historyService.createHistoryTableAction();
                action.accept(generator);
            }

            ChangeLogHistory history = historyService.readChangeLogHistory(dbc);
            generator.setChangeSetFilter(c -> !history.containsKey(c.getId()));

            ChangeLog changeLog = readChangeLog(is);
            changeLog.accept(generator);
        }
        finally {
            dbc.setAutoCommit(autoCommit);
        }
    }

    private void runUpdate(PreparedStatement st) {
        try {
            st.executeUpdate();
        }
        catch (SQLException exc) {
            throw Exceptions.unchecked(exc);
        }
    }

    private ChangeLog readChangeLog(InputStream is) throws JAXBException {
        InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
        ChangeLog changeLog = changeLogReader.parse(reader);
        return changeLog;
    }
}
