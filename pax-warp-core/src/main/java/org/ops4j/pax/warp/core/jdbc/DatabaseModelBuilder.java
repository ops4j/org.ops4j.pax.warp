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
package org.ops4j.pax.warp.core.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import org.ops4j.pax.warp.exc.WarpException;
import org.ops4j.pax.warp.jaxb.gen.AddForeignKey;
import org.ops4j.pax.warp.jaxb.gen.AddPrimaryKey;
import org.ops4j.pax.warp.jaxb.gen.Column;
import org.ops4j.pax.warp.jaxb.gen.ColumnPair;
import org.ops4j.pax.warp.jaxb.gen.ColumnReference;
import org.ops4j.pax.warp.jaxb.gen.CreateIndex;
import org.ops4j.pax.warp.jaxb.gen.CreateTable;
import org.ops4j.pax.warp.jaxb.gen.SqlType;
import org.ops4j.pax.warp.jaxb.gen.TableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a {@link DatabaseModel} for a given relational database.
 *
 * @author Harald Wellmann
 *
 */
public class DatabaseModelBuilder {

    private static Logger log = LoggerFactory.getLogger(DatabaseModelBuilder.class);

    private DatabaseMetaData metaData;

    private String catalog;
    private String schema;

    private Connection dbc;

    private DatabaseModel database;

    /**
     * Constructs a model builder for the given database, working on the default schema.
     *
     * @param dbc
     *            JDBC database connection
     */
    public DatabaseModelBuilder(Connection dbc) {
        this(dbc, null, null);
    }

    /**
     * Constructs a model builder for the given database, working on the given catalog and schema.
     *
     * @param dbc
     *            JDBC database connection
     * @param catalog
     *            database catalog
     * @param schema
     *            database schema
     */
    public DatabaseModelBuilder(Connection dbc, String catalog, String schema) {
        this.dbc = dbc;
        this.catalog = catalog;
        this.schema = schema;
    }

    /**
     * Builds a model of the given database.
     *
     * @return database model
     */
    public DatabaseModel buildDatabaseModel() {
        database = new DatabaseModel();
        try {
            this.metaData = dbc.getMetaData();
            buildTables();
            buildPrimaryKeys();
            buildForeignKeys();
            buildIndexes();
            return database;
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }
    }

    private void buildTables() throws SQLException {
        try (ResultSet rs = metaData.getTables(catalog, schema, null, new String[] { "TABLE" })) {
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                CreateTable createTable = new CreateTable();
                createTable.setCatalogName(catalog);
                createTable.setSchemaName(schema);
                createTable.setTableName(tableName);
                log.debug("table: {}", tableName);
                database.addTable(createTable);
            }
        }

        for (CreateTable table : database.getTables()) {
            buildColumns(table);
        }
    }

    private void buildColumns(CreateTable table) throws SQLException {
        log.debug("columns of table {}", table.getTableName());
        try (ResultSet rs = metaData.getColumns(catalog, schema, table.getTableName(), null)) {
            while (rs.next()) {
                int ordinal = rs.getInt("ORDINAL_POSITION");
                String columnName = rs.getString("COLUMN_NAME");
                int dataType = rs.getInt("DATA_TYPE");
                String typeName = rs.getString("TYPE_NAME");
                int columnSize = rs.getInt("COLUMN_SIZE");
                int decimalDigits = rs.getInt("DECIMAL_DIGITS");
                int nullable = rs.getInt("NULLABLE");
                String autoIncrement = rs.getString("IS_AUTOINCREMENT");
                JDBCType jdbcType = JDBCType.valueOf(dataType);
                log.debug("column [{}]: name={}, jdbcType={}, typeName={}, size={}, digits={}, "
                    + "nullable={}, autoIncrement={}", ordinal, columnName, jdbcType, typeName,
                    columnSize, decimalDigits, nullable, autoIncrement);
                List<Column> columns = table.getColumn();
                Column column = new Column();
                column.setName(columnName);
                SqlType sqlType = convertType(jdbcType);
                if (typeName.equals("text")) {
                    sqlType = SqlType.CLOB;
                }
                column.setType(sqlType);
                if (hasLength(sqlType)) {
                    column.setLength(columnSize);
                }
                if (hasPrecision(sqlType)) {
                    column.setPrecision(columnSize);
                    column.setScale(decimalDigits);
                }
                if (nullable == DatabaseMetaData.columnNoNulls) {
                    column.setNullable(false);
                }
                if ("YES".equals(autoIncrement)) {
                    column.setAutoIncrement(true);
                }
                columns.add(column);
            }
        }
    }

    private void buildForeignKeys() throws SQLException {
        for (CreateTable table : database.getTables()) {
            buildForeignKeys(table);
        }
    }

    private void buildForeignKeys(CreateTable table) throws SQLException {
        String fkName = null;
        AddForeignKey addFk = null;
        List<ColumnPair> columnPairs = new ArrayList<>();
        try (ResultSet rs = metaData.getImportedKeys(catalog, schema, table.getTableName())) {
            while (rs.next()) {

                String pkCatalog = rs.getString("PKTABLE_CAT");
                String pkSchema = rs.getString("PKTABLE_SCHEM");
                String pkTable = rs.getString("PKTABLE_NAME");
                String pkColumn = rs.getString("PKCOLUMN_NAME");
                String fkCatalog = rs.getString("FKTABLE_CAT");
                String fkSchema = rs.getString("FKTABLE_SCHEM");
                String fkTable = rs.getString("FKTABLE_NAME");
                String fkColumn = rs.getString("FKCOLUMN_NAME");
                int keySeq = rs.getShort("KEY_SEQ");
                fkName = rs.getString("FK_NAME");
                log.debug("FK column [{}]: {} {} -> {} {}", keySeq, fkTable, fkColumn, pkTable,
                    pkColumn);

                ColumnPair pair = new ColumnPair();
                ColumnReference baseCol = new ColumnReference();
                baseCol.setColumnName(fkColumn);
                ColumnReference refCol = new ColumnReference();
                refCol.setColumnName(pkColumn);
                pair.setBase(baseCol);
                pair.setReferenced(refCol);

                if (keySeq == 1) {
                    if (addFk != null) {
                        addFk.getColumnPair().addAll(columnPairs);
                        database.getForeignKeys().add(addFk);
                        columnPairs = new ArrayList<>();
                    }
                    addFk = new AddForeignKey();
                    addFk.setConstraintName(fkName);
                    TableReference base = new TableReference();
                    base.setCatalogName(fkCatalog);
                    base.setSchemaName(fkSchema);
                    base.setTableName(fkTable);
                    TableReference ref = new TableReference();
                    ref.setCatalogName(pkCatalog);
                    ref.setSchemaName(pkSchema);
                    ref.setTableName(pkTable);
                    addFk.setBaseTable(base);
                    addFk.setReferencedTable(ref);
                }
                columnPairs.add(pair);
            }
        }
        if (addFk != null) {
            addFk.getColumnPair().addAll(columnPairs);
            database.getForeignKeys().add(addFk);
        }
    }

    private void buildPrimaryKeys() throws SQLException {
        for (CreateTable table : database.getTables()) {
            buildPrimaryKey(table);
        }
    }

    private void buildPrimaryKey(CreateTable table) throws SQLException {
        String tableName = table.getTableName();
        String pkName = null;
        SortedMap<Integer, String> columnMap = new TreeMap<>();
        try (ResultSet rs = metaData.getPrimaryKeys(catalog, schema, tableName)) {
            while (rs.next()) {
                pkName = rs.getString("PK_NAME");
                String columnName = rs.getString("COLUMN_NAME");
                int keySeq = rs.getShort("KEY_SEQ");
                columnMap.put(keySeq, columnName);
                log.debug("PK column [{}]: {} {} {}", keySeq, tableName, pkName, columnName);
            }
        }
        if (!columnMap.isEmpty()) {
            AddPrimaryKey addPk = new AddPrimaryKey();
            addPk.setCatalogName(catalog);
            addPk.setSchemaName(schema);
            addPk.setTableName(tableName);
            if (!"PRIMARY".equals(pkName)) {
                addPk.setConstraintName(pkName);
            }
            addPk.getColumn().addAll(columnMap.values());
            database.getPrimaryKeys().add(addPk);
        }
    }

    private void buildIndexes() throws SQLException {
        for (CreateTable table : database.getTables()) {
            buildIndexes(table);
        }
    }

    private void buildIndexes(CreateTable table) throws SQLException {
        CreateIndex index = null;
        try (ResultSet rs = metaData.getIndexInfo(catalog, schema, table.getTableName(), false,
            false)) {
            while (rs.next()) {
                boolean nonUnique = rs.getBoolean("NON_UNIQUE");
                String indexName = rs.getString("INDEX_NAME");
                int ordinal = rs.getShort("ORDINAL_POSITION");
                String columnName = rs.getString("COLUMN_NAME");
                Column column = new Column();
                column.setName(columnName);
                setKeyLength(table, column);
                if (ordinal == 1) {
                    maybeAddIndex(index);
                    index = new CreateIndex();
                    index.setCatalogName(catalog);
                    index.setSchemaName(schema);
                    index.setTableName(table.getTableName());
                    index.setIndexName(indexName);
                    if (!nonUnique) {
                        index.setUnique(true);
                    }
                }
                index.getColumn().add(column);
            }
        }
        maybeAddIndex(index);
    }

    private void maybeAddIndex(CreateIndex index) {
        if (index != null && !isPrimaryKeyIndex(index)) {
            database.getIndexes().add(index);
        }
    }

    /**
     * @param table
     * @param column
     */
    private void setKeyLength(CreateTable table, Column column) {
        for (Column c : table.getColumn()) {
            if (c.getName().equals(column.getName())) {
                switch (c.getType()) {
                    case BLOB:
                    case CLOB:
                        column.setLength(300);
                        break;

                    default:
                        break;
                }
            }
        }
    }

    /**
     * @param index
     * @return
     */
    private boolean isPrimaryKeyIndex(CreateIndex index) {
        assert index != null;
        for (AddPrimaryKey pk : database.getPrimaryKeys()) {
            assert pk != null;
            if (Objects.equals(index.getIndexName(), pk.getConstraintName())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPrecision(SqlType sqlType) {
        switch (sqlType) {
            case DECIMAL:
            default:
                return false;
        }
    }

    private boolean hasLength(SqlType sqlType) {
        switch (sqlType) {
            case CHAR:
            case VARCHAR:
                return true;
            default:
                return false;
        }
    }

    private SqlType convertType(JDBCType jdbcType) {
        switch (jdbcType) {
            case BIGINT:
                return SqlType.INT_64;
            case BIT:
            case BOOLEAN:
                return SqlType.BOOLEAN;
            case BINARY:
            case BLOB:
            case VARBINARY:
                return SqlType.BLOB;
            case CHAR:
                return SqlType.CHAR;
            case CLOB:
            case LONGVARCHAR:
                return SqlType.CLOB;
            case DATE:
                return SqlType.DATE;
            case DECIMAL:
            case NUMERIC:
                return SqlType.DECIMAL;
            case DOUBLE:
                return SqlType.DOUBLE;
            case INTEGER:
                return SqlType.INT_32;
            case SMALLINT:
                return SqlType.INT_16;
            case TINYINT:
                return SqlType.INT_8;
            case TIME:
                return SqlType.TIME;
            case TIMESTAMP:
            case TIMESTAMP_WITH_TIMEZONE:
                return SqlType.TIMESTAMP;
            case VARCHAR:
                return SqlType.VARCHAR;
            default:
                throw new IllegalArgumentException(jdbcType.toString());
        }
    }

    /**
     * @return the catalog
     */
    public String getCatalog() {
        return catalog;
    }

    /**
     * @param catalog
     *            the catalog to set
     */
    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    /**
     * @return the schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * @param schema
     *            the schema to set
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }
}
