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

import java.util.HashMap;
import java.util.Map;

import org.ops4j.pax.warp.exc.WarpException;
import org.ops4j.pax.warp.scope.CdiDependent;
import org.osgi.service.component.annotations.Component;


/**
 * @author Harald Wellmann
 *
 */
@CdiDependent
@Component(service = DbmsProfileSelector.class)
public class DbmsProfileSelector {

    private static Map<String, DbmsProfile> profileMap = new HashMap<>();

    static  {
        addProfile(new DerbyProfile());
        addProfile(new H2Profile());
        addProfile(new MysqlProfile());
        addProfile(new PostgresProfile());
    }

    public DbmsProfile selectProfile(String subprotocol) {
        DbmsProfile profile = profileMap.get(subprotocol);
        if (profile == null) {
            throw new WarpException("unknown JDBC subprotocol: " + subprotocol);
        }
        else {
            return profile;
        }
    }

    /**
     * @param derbyProfile
     */
    private static void addProfile(DbmsProfile profile) {
        profileMap.put(profile.getSubprotocol(), profile);
    }
}
