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

import java.util.List;

import javax.inject.Named;

import org.ops4j.pax.warp.core.jdbc.DatabaseModel;
import org.ops4j.pax.warp.jaxb.gen.AddForeignKey;
import org.ops4j.pax.warp.jaxb.gen.CreateTable;
import org.ops4j.pax.warp.jaxb.gen.DropForeignKey;
import org.ops4j.pax.warp.jaxb.gen.TruncateTable;
import org.ops4j.pax.warp.scope.CdiDependent;
import org.osgi.service.component.annotations.Component;

/**
 * Performs various operations on a change log model.
 *
 * @author Harald Wellmann
 *
 */
@CdiDependent
@Named
@Component(service = ChangeLogService.class)
public class ChangeLogService {

    /**
     * Adds actions for dropping all foreign keys from the given database model to the given change
     * log model.
     *
     * @param changes
     *            list of changes of a given change set in a change log model
     * @param database
     *            database model
     */
    public void dropForeignKeys(List<Object> changes, DatabaseModel database) {
        for (AddForeignKey addFk : database.getForeignKeys()) {
            DropForeignKey dropFk = new DropForeignKey();
            dropFk.setBaseTable(addFk.getBaseTable());
            dropFk.setConstraintName(addFk.getConstraintName());
            changes.add(dropFk);
        }
    }

    /**
     * Adds actions for truncating all tables from the given database model to the given change
     * log model.
     *
     * @param changes
     *            list of changes of a given change set in a change log model
     * @param database
     *            database model
     */
    public void truncateTables(List<Object> changes, DatabaseModel database) {
        for (CreateTable createTable : database.getTables()) {
            TruncateTable truncateTable = new TruncateTable();
            truncateTable.setCatalogName(createTable.getCatalogName());
            truncateTable.setSchemaName(createTable.getSchemaName());
            truncateTable.setTableName(createTable.getTableName());
            changes.add(truncateTable);
        }
    }
}
