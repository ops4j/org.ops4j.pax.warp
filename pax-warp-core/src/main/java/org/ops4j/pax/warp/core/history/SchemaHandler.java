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
package org.ops4j.pax.warp.core.history;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;

import org.ops4j.pax.warp.core.changelog.impl.BaseSqlGenerator;
import org.ops4j.pax.warp.core.dbms.DbmsProfile;

/**
 * @author Harald Wellmann
 *
 */
public class SchemaHandler extends BaseSqlGenerator {

    /**
     * @param dbms
     * @param dbc
     * @param consumer
     */
    public SchemaHandler(DbmsProfile dbms, Connection dbc,
        Consumer<PreparedStatement> consumer) {
        super(dbms, dbc, consumer);
    }

    public String getCurrentSchema() throws SQLException {
        String sql = renderTemplate("getCurrentSchema", new Object());
        Statement st = dbc.createStatement();
        ResultSet rs = st.executeQuery(sql);
        String schema = null;
        if (rs.next()) {
            schema = rs.getString(1);
        }
        rs.close();
        st.close();
        return schema;
    }

    public void setCurrentSchema(String schemaName) {

    }

}
