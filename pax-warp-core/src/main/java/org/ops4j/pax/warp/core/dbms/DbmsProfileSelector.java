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
package org.ops4j.pax.warp.core.dbms;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;

import org.ops4j.pax.warp.exc.WarpException;
import org.ops4j.pax.warp.scope.CdiDependent;
import org.osgi.service.component.annotations.Component;

/**
 * Selects the appropriate {@link DbmsProfile} for a database.
 *
 * @author Harald Wellmann
 *
 */
@CdiDependent
@Component(service = DbmsProfileSelector.class)
@Named
public class DbmsProfileSelector {

    private static Map<String, DbmsProfile> profileMap = new HashMap<>();

    static {
        addProfile(new DerbyProfile());
        addProfile(new H2Profile());
        addProfile(new MysqlProfile());
        addProfile(new OracleProfile());
        addProfile(new PostgresProfile());
    }

    /**
     * Selects the DBMS profile for the given JDBC subprotocol.
     *
     * @param subprotocol
     *            JDBC subprotocol
     * @return profile
     * @throws WarpException
     *             if no profile is found
     */
    public DbmsProfile selectProfile(String subprotocol) {
        DbmsProfile profile = profileMap.get(subprotocol);
        if (profile == null) {
            throw new WarpException("unknown JDBC subprotocol: " + subprotocol);
        }
        else {
            return profile;
        }
    }

    private String getSubprotocol(String jdbcUrl) {
        String[] words = jdbcUrl.split(":", 3);
        return words[1];
    }

    /**
     * Select the profile for the given connection.
     *
     * @param dbc
     *            database connection
     * @return profile
     */
    public DbmsProfile selectProfile(Connection dbc) {
        try {
            String subprotocol = getSubprotocol(dbc.getMetaData().getURL());
            return selectProfile(subprotocol);
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }
    }

    private static void addProfile(DbmsProfile profile) {
        profileMap.put(profile.getSubprotocol(), profile);
    }
}
