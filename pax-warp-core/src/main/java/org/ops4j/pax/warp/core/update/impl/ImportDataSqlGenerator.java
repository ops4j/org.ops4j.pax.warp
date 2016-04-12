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
package org.ops4j.pax.warp.core.update.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.ops4j.pax.warp.core.dbms.DbmsProfile;
import org.ops4j.pax.warp.exc.WarpException;
import org.ops4j.pax.warp.jaxb.WarpJaxbContext;
import org.ops4j.pax.warp.jaxb.gen.AddColumn;
import org.ops4j.pax.warp.jaxb.gen.AddForeignKey;
import org.ops4j.pax.warp.jaxb.gen.AddPrimaryKey;
import org.ops4j.pax.warp.jaxb.gen.CreateIndex;
import org.ops4j.pax.warp.jaxb.gen.CreateTable;
import org.ops4j.pax.warp.jaxb.gen.DropColumn;
import org.ops4j.pax.warp.jaxb.gen.DropForeignKey;
import org.ops4j.pax.warp.jaxb.gen.DropIndex;
import org.ops4j.pax.warp.jaxb.gen.DropPrimaryKey;
import org.ops4j.pax.warp.jaxb.gen.Insert;
import org.ops4j.pax.warp.jaxb.gen.RunSql;
import org.ops4j.pax.warp.jaxb.gen.TruncateTable;
import org.ops4j.pax.warp.jaxb.gen.visitor.Visitable;
import org.ops4j.pax.warp.jaxb.gen.visitor.VisitorAction;

/**
 * SQL generator for {@code importData} command. Only supports {@code INSERT} and throws exceptions
 * for all other changes.
 *
 * @author Harald Wellmann
 *
 */
public class ImportDataSqlGenerator extends InsertSqlGenerator {

    /**
     * Creates a new import SQL generator.
     *
     * @param dbms
     *            DBMS profile
     * @param dbc
     *            database connection
     * @param consumer
     *            prepared statement consumer
     * @param context
     *            JAXB context for Warp schema
     */
    public ImportDataSqlGenerator(DbmsProfile dbms, Connection dbc,
        Consumer<PreparedStatement> consumer,
        WarpJaxbContext context) {
        super(dbms, dbc, consumer, context);
    }

    private VisitorAction notSupported(Visitable action) {
        String actionName = action.getClass().getName();
        actionName = Character.toLowerCase(actionName.charAt(0)) + actionName.substring(1);
        throw new WarpException(actionName + " action is not supported for importData command");
    }

    @Override
    public VisitorAction enter(CreateTable action) {
        return notSupported(action);
    }

    @Override
    public VisitorAction enter(AddPrimaryKey action) {
        return notSupported(action);
    }

    @Override
    public VisitorAction enter(AddForeignKey action) {
        return notSupported(action);
    }

    @Override
    public VisitorAction enter(DropForeignKey action) {
        return notSupported(action);
    }

    @Override
    public VisitorAction enter(DropPrimaryKey action) {
        return notSupported(action);
    }

    @Override
    public VisitorAction enter(CreateIndex action) {
        return notSupported(action);
    }

    @Override
    public VisitorAction enter(DropIndex action) {
        return notSupported(action);
    }

    @Override
    public VisitorAction enter(AddColumn action) {
        return notSupported(action);
    }

    @Override
    public VisitorAction enter(DropColumn action) {
        return notSupported(action);
    }

    @Override
    public VisitorAction enter(Insert action) {
        return generateInsert(action);
    }

    @Override
    public VisitorAction enter(TruncateTable action) {
        return notSupported(action);
    }

    @Override
    public VisitorAction enter(RunSql action) {
        return notSupported(action);
    }

    protected void resetSequences(String schema) {
        Map<String, String> params = new HashMap<>();
        params.put("schemaName", schema);
        produceStatement("resetSequenceFunction", params);
        produceStatement("resetSequences", params);
    }
}
