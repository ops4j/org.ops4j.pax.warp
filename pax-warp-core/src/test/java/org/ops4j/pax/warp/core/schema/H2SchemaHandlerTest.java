/*
 * Copyright 2016 Harald Wellmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 *
 * See the License for the specific language governing permissions and limitations under the
 * License.
 */
package org.ops4j.pax.warp.core.schema;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Harald Wellmann
 *
 */
public class H2SchemaHandlerTest extends AbstractSchemaHandlerTest {

    @Override
    protected String getJdbcUrl() {
        return "jdbc:h2:mem:warp;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    }

    @Override
    protected String getJdbcAdminUrl() {
        return null;
    }

    @Override
    protected String getSubprotocol() {
        return "h2";
    }
    @Override
    protected void dropAndCreateDatabase() throws SQLException {
        try (Connection dbc = DriverManager.getConnection(getJdbcUrl(), "warp", "warp");
            Statement st = dbc.createStatement()) {
            st.execute("drop all objects");
            dbc.commit();
        }
    }

}
