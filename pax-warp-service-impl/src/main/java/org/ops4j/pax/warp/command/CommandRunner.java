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
package org.ops4j.pax.warp.command;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.ops4j.pax.warp.changelog.DatabaseChangeLogWriter;
import org.ops4j.pax.warp.changelog.impl.JaxbDatabaseChangeLogReader;
import org.ops4j.pax.warp.changelog.impl.JaxbDatabaseChangeLogWriter;
import org.ops4j.pax.warp.changelog.impl.UpdateSqlGenerator;
import org.ops4j.pax.warp.jaxb.ChangeSet;
import org.ops4j.pax.warp.jaxb.DatabaseChangeLog;
import org.ops4j.pax.warp.jdbc.Database;
import org.ops4j.pax.warp.jdbc.MetaDataInspector;

/**
 * @author Harald Wellmann
 *
 */
public class CommandRunner {

    public void dump(String jdbcUrl, String username, String password, OutputStream os)
        throws SQLException, JAXBException {
        Connection dbc = DriverManager.getConnection(jdbcUrl, username, password);
        MetaDataInspector inspector = new MetaDataInspector(dbc, null, null);
        Database database = inspector.buildDatabaseModel();

        DatabaseChangeLog changeLog = new DatabaseChangeLog();
        changeLog.setLogicalFilePath("changelog.xml");
        changeLog.getChangeSetOrInclude();

        ChangeSet changeSet = new ChangeSet();
        List<Object> changes = changeSet.getCreateTableOrAddPrimaryKeyOrAddForeignKey();
        changes.addAll(database.getTables());
        changes.addAll(database.getPrimaryKeys());
        changes.addAll(database.getForeignKeys());
        changeLog.getChangeSetOrInclude().add(changeSet);

        DatabaseChangeLogWriter changeLogWriter = new JaxbDatabaseChangeLogWriter();
        OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
        changeLogWriter.write(changeLog, writer);
    }

    public void update(String jdbcUrl, String username, String password, InputStream is) throws JAXBException {
        JaxbDatabaseChangeLogReader changeLogReader = new JaxbDatabaseChangeLogReader();
        InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
        DatabaseChangeLog changeLog = changeLogReader.parse(reader);

        UpdateSqlGenerator generator = new UpdateSqlGenerator("mysql", System.out::println);
        changeLog.accept(generator);

    }

}
