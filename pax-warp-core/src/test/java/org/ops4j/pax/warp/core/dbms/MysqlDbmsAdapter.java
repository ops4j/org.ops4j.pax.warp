/*
 * Copyright 2014 Harald Wellmann.
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
package org.ops4j.pax.warp.core.dbms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Harald Wellmann
 *
 */
public class MysqlDbmsAdapter implements DbmsAdapter {

    @Override
    public String getJdbcUrl() {
        return "jdbc:mysql://localhost/warp?useSSL=false";
    }

    @Override
    public String getJdbcAdminUrl() {
        return "jdbc:mysql://localhost/warp_admin?useSSL=false";
    }

    @Override
    public void dropAndCreateDatabase() throws SQLException {
        Connection dbc = DriverManager.getConnection(getJdbcAdminUrl(), "warp", "warp");
        Statement st = dbc.createStatement();
        st.executeUpdate("drop database if exists warp");
        st.executeUpdate("drop database if exists foo");
        st.executeUpdate("create database warp");
        st.close();
        dbc.close();
    }

    @Override
    public String getSubprotocol() {
        return "mysql";
    }
}
