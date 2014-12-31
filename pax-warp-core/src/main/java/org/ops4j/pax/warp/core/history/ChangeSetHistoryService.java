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
package org.ops4j.pax.warp.core.history;

import java.sql.Connection;
import java.sql.SQLException;

import org.ops4j.pax.warp.jaxb.gen.CreateTable;

/**
 * Manages the change set history of a database. The history is stored in a metadata table named
 * {@code warp_history} which gets created by this service when required.
 *
 * @author Harald Wellmann
 *
 */
public interface ChangeSetHistoryService {

    /**
     * Creates a change action for creating the history metadata table.
     *
     * @return create table action
     */
    CreateTable createHistoryTableAction();

    /**
     * Checks if the given database already contains a change set history table.
     *
     * @param dbc
     *            JDBC database connection
     * @return true if the change set history table exists
     * @throws SQLException
     */
    boolean hasMetaDataTable(Connection dbc) throws SQLException;

    /**
     * Reads the change set history from the given database.
     *
     * @param dbc
     *            JDBC database connection
     * @return change set history
     */
    ChangeSetHistory readChangeSetHistory(Connection dbc);
}
