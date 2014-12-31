package org.ops4j.pax.warp.core.jdbc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.ops4j.pax.warp.jaxb.gen.AddForeignKey;
import org.ops4j.pax.warp.jaxb.gen.AddPrimaryKey;
import org.ops4j.pax.warp.jaxb.gen.CreateIndex;
import org.ops4j.pax.warp.jaxb.gen.CreateTable;

/**
 * A structural model of a relational database, containing table, primary key, foreign key and index
 * definitions.
 *
 * @author Harald Wellmann
 *
 */
public class DatabaseModel {

    private SortedMap<String, CreateTable> tables = new TreeMap<>();

    private List<AddPrimaryKey> primaryKeys = new ArrayList<>();

    private List<AddForeignKey> foreignKeys = new ArrayList<>();

    private List<CreateIndex> indexes = new ArrayList<>();

    /**
     * Returns all tables in alphabetical order.
     *
     * @return list of create table actions, possibly empty but not null
     */
    public Collection<CreateTable> getTables() {
        return tables.values();
    }

    /**
     * Adds a table to the model.
     *
     * @param table
     *            create table action
     */
    public void addTable(CreateTable table) {
        tables.put(table.getTableName(), table);
    }

    /**
     * Gets the create action for the table with the given name from the default schema.
     *
     * @param tableName
     *            table name
     * @return create action, or null, if table does not exist
     */
    public CreateTable getTable(String tableName) {
        return tables.get(tableName);
    }

    /**
     * Removes a table from the model.
     *
     * @param tableName
     *            table name
     * @return create table action, or null, if table does not exist
     */
    public CreateTable removeTable(String tableName) {
        return tables.remove(tableName);
    }

    /**
     * Gets all primary keys of the given database.
     *
     * @return list of add primary key actions, possibly empty but not null
     */
    public List<AddPrimaryKey> getPrimaryKeys() {
        return primaryKeys;
    }

    /**
     * Sets the list of primary keys of the given database.
     *
     * @param primaryKeys
     *            list of primary keys (possibly empty but not null)
     */
    public void setPrimaryKeys(List<AddPrimaryKey> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }

    /**
     * Gets all foreign keys of the given database.
     *
     * @return list of add foreign key actions, possibly empty but not null
     */
    public List<AddForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    /**
     * Sets the list of foreign keys of the given database.
     *
     * @param foreignKeys
     *            list of foreign keys (possibly empty but not null)
     */
    public void setForeignKeys(List<AddForeignKey> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    /**
     * Gets all indexes of the given database.
     *
     * @return list of create index actions, possibly empty but not null
     */
    public List<CreateIndex> getIndexes() {
        return indexes;
    }

    /**
     * Sets the list of indexes of the given database.
     *
     * @param indexes
     *            list of create index actions (possibly empty but not null)
     */
    public void setIndexes(List<CreateIndex> indexes) {
        this.indexes = indexes;
    }
}
