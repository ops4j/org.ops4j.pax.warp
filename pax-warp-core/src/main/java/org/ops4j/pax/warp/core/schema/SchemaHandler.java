/*
 * Copyright 2015 Harald Wellmann.
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
package org.ops4j.pax.warp.core.schema;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.ops4j.pax.warp.core.trimou.TemplateEngine;
import org.ops4j.pax.warp.exc.WarpException;

/**
 * Gets or sets the current schema on a database connection.
 *
 * @author Harald Wellmann
 *
 */
public class SchemaHandler {

    private TemplateEngine engine;

    /**
     * Constructs a schema handler for the given JDBC subprotocol.
     *
     * @param subprotocol
     *            JDBC subprotocol
     */
    public SchemaHandler(String subprotocol) {
        this.engine = new TemplateEngine(subprotocol);
    }

    /**
     * Gets the current schema set on the given connection.
     *
     * @param dbc
     *            database connection
     * @return current schema
     * @throws SQLException
     */
    public String getCurrentSchema(Connection dbc) {
        String sql = engine.renderTemplate("getCurrentSchema", new Object());
        try (Statement st = dbc.createStatement();
            ResultSet rs = st.executeQuery(sql)) {
            String schema = null;
            if (rs.next()) {
                schema = rs.getString(1);
            }
            return schema;
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }
    }

    /**
     * Sets the current schema on the given connection.
     *
     * @param dbc
     *            database connection
     * @param schemaName
     *            name of schema to be set
     */
    public void setCurrentSchema(Connection dbc, String schemaName) {
        runSql(dbc, engine.renderTemplate("setCurrentSchema", schemaName));
    }

    private void runSql(Connection dbc, String sql) {
        try (Statement st = dbc.createStatement()) {
            st.execute(sql);
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }
    }

    /**
     * Creates the given schema if it does not exist and sets it on the given connection.
     *
     * @param dbc
     *            database connection
     * @param schemaName
     *            name of schema to be set
     */
    public void createAndSetSchema(Connection dbc, String schemaName) {
        runSql(dbc, engine.renderTemplate("createSchema", schemaName));
        setCurrentSchema(dbc, schemaName);
    }
}
