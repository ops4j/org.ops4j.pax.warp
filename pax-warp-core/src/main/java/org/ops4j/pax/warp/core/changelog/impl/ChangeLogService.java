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
package org.ops4j.pax.warp.core.changelog.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.ops4j.pax.warp.core.changelog.ChangeLogWriter;
import org.ops4j.pax.warp.core.jdbc.DatabaseModel;
import org.ops4j.pax.warp.exc.WarpException;
import org.ops4j.pax.warp.jaxb.gen.AddForeignKey;
import org.ops4j.pax.warp.jaxb.gen.ChangeLog;
import org.ops4j.pax.warp.jaxb.gen.CreateTable;
import org.ops4j.pax.warp.jaxb.gen.DropForeignKey;
import org.ops4j.pax.warp.jaxb.gen.TruncateTable;
import org.ops4j.pax.warp.scope.CdiDependent;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;


/**
 * @author Harald Wellmann
 *
 */
@CdiDependent
@Named
@Component(service = ChangeLogService.class)
public class ChangeLogService {

    @Inject
    private ChangeLogWriter changeLogWriter;

    /**
     * @param changes
     * @param database
     */
    public void dropForeignKeys(List<Object> changes, DatabaseModel database) {
        for (AddForeignKey addFk : database.getForeignKeys()) {
            DropForeignKey dropFk = new DropForeignKey();
            dropFk.setBaseTable(addFk.getBaseTable());
            dropFk.setConstraintName(addFk.getConstraintName());
            changes.add(dropFk);
        }
    }

    public void truncateTables(List<Object> changes, DatabaseModel database) {
        for (CreateTable createTable : database.getTables()) {
            TruncateTable truncateTable = new TruncateTable();
            truncateTable.setCatalogName(createTable.getCatalogName());
            truncateTable.setSchemaName(createTable.getSchemaName());
            truncateTable.setTableName(createTable.getTableName());
            changes.add(truncateTable);
        }
    }

    public void writeChangeLog(ChangeLog changeLog, File outputFile) {
        try (OutputStream os = new FileOutputStream(outputFile)) {
            writeChangeLog(changeLog, os);
        }
        catch (IOException exc) {
            throw new WarpException(exc);
        }
    }

    public void writeChangeLog(ChangeLog changeLog, OutputStream os) {
        OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
        changeLogWriter.write(changeLog, writer);
    }


    /**
     * @param changeLogWriter the changeLogWriter to set
     */
    @Reference
    public void setChangeLogWriter(ChangeLogWriter changeLogWriter) {
        this.changeLogWriter = changeLogWriter;
    }
}
