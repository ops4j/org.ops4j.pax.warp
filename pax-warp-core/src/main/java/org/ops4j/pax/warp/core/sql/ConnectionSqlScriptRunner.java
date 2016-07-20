/*
 * Copyright 2013 Harald Wellmann.
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
package org.ops4j.pax.warp.core.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ops4j.pax.warp.core.exc.Exceptions;

/**
 * Executes an SQL script on a given Connection.
 * 
 * @author Harald Wellmann
 * 
 */
public class ConnectionSqlScriptRunner extends AbstractSqlScriptRunner {

    private static Logger log = LoggerFactory.getLogger(ConnectionSqlScriptRunner.class);

    private Connection dbc;

    /**
     * Creates a script runner for the given database connection.
     * 
     * @param dbc
     *            database connection
     */
    public ConnectionSqlScriptRunner(Connection dbc) {
        this.dbc = dbc;
    }

    /**
     * Runs a single statement from the script.
     * 
     * @param sql
     *            SQL statement
     */
    protected void runStatement(String sql) {
        log.info("running SQL statement\n{};", sql);
        try (Statement st = dbc.createStatement()) {
            st.executeUpdate(sql);
        }
        catch (SQLException exc) {
            if (getTerminateOnError()) {
                throw Exceptions.unchecked(exc);
            }
            log.error("error in SQL statement", exc);
        }
    }
}
