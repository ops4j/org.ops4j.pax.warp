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
package org.ops4j.pax.warp.core.dbms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Harald Wellmann
 *
 */
public class OracleDbmsAdapter implements DbmsAdapter {
    
    private String getPort() {
        return System.getProperty("oracle.jdbc.port", "1521");
    }

    @Override
    public String getJdbcUrl() {
        return String.format("jdbc:oracle:thin://@localhost:%s:xe", getPort());
    }

    @Override
    public String getJdbcAdminUrl() {
        return String.format("jdbc:oracle:thin://@localhost:%s:xe", getPort());
    }

    @Override
    public void dropAndCreateDatabase() throws SQLException {
        Connection dbc = DriverManager.getConnection(getJdbcAdminUrl(), "warp_admin", "warp_admin");
        Statement st = dbc.createStatement();
        tryExecute(st, "drop user warp cascade");
        tryExecute(st, "drop user foo cascade");
        st.executeUpdate("create user warp identified by \"warp\"");
        st.executeUpdate("grant dba to warp");
        st.close();
        dbc.close();
    }
    
    private void tryExecute(Statement st, String sql) {
        try {
            st.execute(sql);
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public String getSubprotocol() {
        return "oracle";
    }
}
