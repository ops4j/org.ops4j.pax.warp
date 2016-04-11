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
package org.ops4j.pax.warp.core.history.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.ops4j.pax.warp.core.dbms.DbmsProfile;
import org.ops4j.pax.warp.core.dbms.DbmsProfileSelector;
import org.ops4j.pax.warp.core.history.ChangeSetHistory;
import org.ops4j.pax.warp.core.history.ChangeSetHistoryService;
import org.ops4j.pax.warp.core.schema.SchemaHandler;
import org.ops4j.pax.warp.exc.WarpException;
import org.ops4j.pax.warp.jaxb.gen.Column;
import org.ops4j.pax.warp.jaxb.gen.CreateTable;
import org.ops4j.pax.warp.jaxb.gen.SqlType;
import org.ops4j.pax.warp.scope.CdiDependent;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Implements {@link ChangeSetHistoryService}.
 *
 * @author Harald Wellmann
 *
 */
@Component
@Named
@CdiDependent
public class ChangeSetHistoryServiceImpl implements ChangeSetHistoryService {

    @Inject
    private DbmsProfileSelector profileSelector;

    @Override
    public CreateTable createHistoryTableAction() {
        CreateTable action = new CreateTable();
        action.setTableName("warp_history");
        List<Column> columns = action.getColumn();
        Column id = new Column();
        id.setName("id");
        id.setType(SqlType.VARCHAR);
        id.setLength(40);
        id.setNullable(false);
        columns.add(id);

        Column checksum = new Column();
        checksum.setName("checksum");
        checksum.setType(SqlType.CHAR);
        checksum.setLength(64);
        checksum.setNullable(false);
        columns.add(checksum);

        Column executed = new Column();
        executed.setName("executed");
        executed.setType(SqlType.TIMESTAMP);
        columns.add(executed);

        return action;
    }

    @Override
    public boolean hasMetaDataTable(Connection dbc) {
        try {
            DatabaseMetaData metaData = dbc.getMetaData();
            String tableName = "warp_history";
            if (metaData.storesUpperCaseIdentifiers()) {
                tableName = tableName.toUpperCase();
            }
            return hasTable(metaData, getSchema(dbc), tableName);
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }
    }

    private String getSchema(Connection dbc) throws SQLException {
        DbmsProfile dbms = profileSelector.selectProfile(dbc);
        SchemaHandler handler = new SchemaHandler(dbms.getSubprotocol());
        return handler.getCurrentSchema(dbc);
    }

    private boolean hasTable(DatabaseMetaData metaData, String schemaName, String tableName)
        throws SQLException {
        boolean result = false;
        try (ResultSet rs = metaData.getTables(null, schemaName, tableName,
            new String[] { "TABLE" })) {
            result = rs.next();
        }
        return result;
    }

    @Override
    public ChangeSetHistory readChangeSetHistory(Connection dbc) {
        ChangeSetHistory history = new ChangeSetHistory();
        try (Statement st = dbc.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, checksum FROM warp_history")) {
            while (rs.next()) {
                String id = rs.getString("id");
                String checksum = rs.getString("checksum");
                history.put(id, checksum);
            }
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }
        return history;
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
