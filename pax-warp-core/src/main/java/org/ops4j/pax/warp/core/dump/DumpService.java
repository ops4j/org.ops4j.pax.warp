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
package org.ops4j.pax.warp.core.dump;

import java.io.OutputStream;
import java.sql.Connection;
import java.util.Optional;

import org.ops4j.pax.warp.core.dbms.DbmsProfile;

/**
 * Dumps structure information or data from a database to a change log.
 *
 * @author Harald Wellmann
 *
 */
public interface DumpService {

    /**
     * Dumps structure information from the given database as an XML change log to the given output
     * stream.
     *
     * @param dbc
     *            JDBC database connection
     * @param os
     *            output stream
     * @param dbms
     *            DBMS profile
     * @param schema
     *            Optional database schema. If missing, the current schema will be used.
     *            If present, the given schema will be used.
     */
    void dumpStructure(Connection dbc, OutputStream os, DbmsProfile dbms, Optional<String> schema);

    /**
     * Dumps all data from the given database as an XML change log to the given output stream.
     *
     * @param dbc
     *            JDBC database connection
     * @param os
     *            output stream
     * @param dbms
     *            DBMS profile
     * @param schema
     *            Optional database schema. If missing, the current schema will be used.
     *            If present, the given schema will be used.
     */
    void dumpData(Connection dbc, OutputStream os, DbmsProfile dbms, Optional<String> schema);
}
