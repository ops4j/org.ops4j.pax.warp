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
package org.ops4j.pax.warp.core.update.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.ops4j.pax.warp.core.dbms.DbmsProfile;
import org.ops4j.pax.warp.core.history.ChangeSetHistory;
import org.ops4j.pax.warp.exc.WarpException;
import org.ops4j.pax.warp.jaxb.WarpJaxbContext;
import org.ops4j.pax.warp.jaxb.gen.AddColumn;
import org.ops4j.pax.warp.jaxb.gen.AddForeignKey;
import org.ops4j.pax.warp.jaxb.gen.AddPrimaryKey;
import org.ops4j.pax.warp.jaxb.gen.ChangeSet;
import org.ops4j.pax.warp.jaxb.gen.Column;
import org.ops4j.pax.warp.jaxb.gen.CreateIndex;
import org.ops4j.pax.warp.jaxb.gen.CreateTable;
import org.ops4j.pax.warp.jaxb.gen.DropColumn;
import org.ops4j.pax.warp.jaxb.gen.DropForeignKey;
import org.ops4j.pax.warp.jaxb.gen.DropIndex;
import org.ops4j.pax.warp.jaxb.gen.DropPrimaryKey;
import org.ops4j.pax.warp.jaxb.gen.DropTable;
import org.ops4j.pax.warp.jaxb.gen.Insert;
import org.ops4j.pax.warp.jaxb.gen.RenameColumn;
import org.ops4j.pax.warp.jaxb.gen.RenameTable;
import org.ops4j.pax.warp.jaxb.gen.RunSql;
import org.ops4j.pax.warp.jaxb.gen.TruncateTable;
import org.ops4j.pax.warp.jaxb.gen.visitor.VisitorAction;
import org.trimou.util.ImmutableMap;

/**
 * SQL generator for DDL statements.
 *
 * @author Harald Wellmann
 *
 */
public class UpdateSqlGenerator extends InsertSqlGenerator {

    private String actualChecksum;
    private ChangeSetHistory changeLogHistory;
    private Set<String> autoIncrementColumns;
    private boolean changeSetSkipped;

    /**
     * Creates a new update SQL generator.
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
    public UpdateSqlGenerator(DbmsProfile dbms, Connection dbc, Consumer<PreparedStatement> consumer,
        WarpJaxbContext context) {
        super(dbms, dbc, consumer, context);
        this.autoIncrementColumns = new HashSet<>();
    }

    @Override
    public VisitorAction enter(CreateTable action) {
        if (dbms.getAutoIncrementIsPrimaryKey()) {
            action.getColumn().stream().filter(c -> isAutoIncrement(c))
                .forEach(c -> autoIncrementColumns.add(action.getTableName() + "." + c.getName()));
        }
        action.getColumn().stream().forEach(this::quoteDefaultValue);
        produceStatement("createTable", action);
        Optional<Column> autoIncr = action.getColumn().stream().filter(this::isAutoIncrement)
            .findFirst();
        if (autoIncr.isPresent()) {
            if (dbms.getAutoIncrementNeedsSequence()) {
                String sql = engine.renderTemplate("createAutoIncrementSequence", action);
                runStatement(sql);
            }
            if (dbms.getAutoIncrementNeedsTrigger()) {
                String sql = engine.renderTemplate("createAutoIncrementTrigger",
                    ImmutableMap.of("action", action, "autoIncrementColumn", autoIncr.get().getName()));
                runSimpleStatement(sql);
            }
        }
        return VisitorAction.CONTINUE;
    }

    private void quoteDefaultValue(Column c) {
        if (c.getDefaultValue() == null) {
            return;
        }

        switch (c.getType()) {
            case CHAR:
            case VARCHAR:
                c.setDefaultValue(quotedString(c.getDefaultValue()));
                break;
            case BOOLEAN:
                if (c.getDefaultValue().contains("1")) {
                    c.setDefaultValue("1");
                }
                else {
                    c.setDefaultValue("0");
                }
                break;
            default:
                // nothing
        }
    }

    private static String quotedString(String s) {
        StringBuilder builder = new StringBuilder();
        builder.append("'");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'') {
                builder.append("''");
            }
            else {
                builder.append(c);
            }
        }
        builder.append("'");
        return builder.toString();
    }

    private boolean isAutoIncrement(Column c) {
        if (c.isAutoIncrement() == null) {
            return false;
        }
        return c.isAutoIncrement();
    }

    @Override
    public VisitorAction enter(AddPrimaryKey action) {
        if (dbms.getAutoIncrementIsPrimaryKey()) {
            String columnKey = action.getTableName() + "." + action.getColumn().get(0);
            if (autoIncrementColumns.contains(columnKey)) {
                return VisitorAction.SKIP;
            }
        }
        return produceStatement("addPrimaryKey", action);
    }

    @Override
    public VisitorAction enter(AddForeignKey action) {
        return produceStatement("addForeignKey", action);
    }

    @Override
    public VisitorAction enter(DropForeignKey action) {
        return produceStatement("dropForeignKey", action);
    }

    @Override
    public VisitorAction enter(DropPrimaryKey action) {
        return produceStatement("dropPrimaryKey", action);
    }

    @Override
    public VisitorAction enter(CreateIndex action) {
        return produceStatement("createIndex", action);
    }

    @Override
    public VisitorAction enter(DropIndex action) {
        return produceStatement("dropIndex", action);
    }

    @Override
    public VisitorAction enter(AddColumn action) {
        return produceStatement("addColumn", action);
    }

    @Override
    public VisitorAction enter(DropColumn action) {
        return produceStatement("dropColumn", action);
    }

    @Override
    public VisitorAction enter(Insert action) {
        return generateInsert(action);
    }

    @Override
    public VisitorAction enter(TruncateTable action) {
        return produceStatement("truncateTable", action);
    }

    @Override
    public VisitorAction enter(RunSql action) {
        String selectedDbms = action.getDbms();
        String currentSubprotocol = dbms.getSubprotocol();

        if (selectedDbms == null || currentSubprotocol.contains(selectedDbms)) {
            String sql = action.getValue();
            runStatement(sql);
        }
        return VisitorAction.CONTINUE;
    }

    @Override
    public VisitorAction enter(RenameColumn action) {
        return produceStatement("renameColumn", action);
    }

    @Override
    public VisitorAction enter(RenameTable action) {
        return produceStatement("renameTable", action);
    }

    @Override
    public VisitorAction enter(DropTable action) {
        return produceStatement("dropTable", action);
    }

    @Override
    public VisitorAction enter(ChangeSet changeSet) {
        actualChecksum = computeChecksum(changeSet);
        String id = changeSet.getId();
        // expectedChecksum may be padded with spaces due to CHAR column - should we use VARCHAR?
        String expectedChecksum = changeLogHistory.get(id);
        if (expectedChecksum != null && !expectedChecksum.trim().equals(actualChecksum)) {
            String msg = String.format("checksum mismatch for change set [id=%s]", id);
            throw new IllegalArgumentException(msg);
        }
        if (!changeSetFilter.test(changeSet)) {
            changeSetSkipped = true;
            return VisitorAction.SKIP;
        }
        changeSetSkipped = false;
        return VisitorAction.CONTINUE;
    }

    @Override
    public VisitorAction leave(ChangeSet changeSet) {
        if (changeSetSkipped) {
            return VisitorAction.CONTINUE;
        }
        try {
            PreparedStatement st = dbc
                .prepareStatement("insert into warp_history (id, checksum, executed) values (?, ?, ?)");
            setValues(st, changeSet.getId(), actualChecksum,
                new Timestamp(System.currentTimeMillis()));
            st.executeUpdate();
            st.close();
            dbc.commit();
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }
        return VisitorAction.CONTINUE;
    }

    private static void setValues(PreparedStatement preparedStatement, Object... values)
        throws SQLException {
        for (int i = 0; i < values.length; i++) {
            preparedStatement.setObject(i + 1, values[i]);
        }
    }

    public void setChangeLogHistory(ChangeSetHistory history) {
        this.changeLogHistory = history;
    }
}
