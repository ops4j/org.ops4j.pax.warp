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
import java.sql.Connection;
import java.util.List;

/**
 * Updates a database with information from a change log.
 *
 * @author Harald Wellmann
 *
 */
public interface UpdateService {

    /**
     * Applies all new change sets from the given change log to the given database. The change set
     * history metadata table is checked for each change set. If the change set identity is stored
     * in the table, the actual change set checksum is compared to the change set checksum stored in
     * the database. If the checksum matches, the change set will be skipped, otherwise, the
     * migration will be aborted. If the change set identity is not stored, the change set will be
     * applied, and a record will be added to the history table.
     *
     * @param dbc
     *            JDBC database connection
     * @param is
     *            change log input stream
     * @param dbms
     *            JDBC subprotocol, identifying a database management system
     */
    void migrate(Connection dbc, InputStream is, String dbms);

    /**
     * Imports all data from the given change log into the given database. The data may come in any
     * order, which might violate foreign keys constraints. To avoid that, all constraints will be
     * disabled, all tables except the excluded ones will be truncated, data will be inserted and
     * finally, the constraints will be enabled again.
     * <p>
     * If the database management system does not natively support disabling constraints, all
     * constraints will be dropped and recreated.
     *
     * @param dbc
     *            JDBC database connection
     * @param is
     *            change log input stream
     * @param dbms
     *            JDBC subprotocol, identifying a database management system
     * @param excludedTables
     *            list of names of tables which will not be truncated
     */
    void importData(Connection dbc, InputStream is, String dbms, List<String> excludedTables);
}
