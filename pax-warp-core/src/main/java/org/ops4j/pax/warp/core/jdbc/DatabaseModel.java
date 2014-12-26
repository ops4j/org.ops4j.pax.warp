package org.ops4j.pax.warp.core.jdbc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ops4j.pax.warp.jaxb.gen.AddForeignKey;
import org.ops4j.pax.warp.jaxb.gen.AddPrimaryKey;
import org.ops4j.pax.warp.jaxb.gen.CreateTable;


public class DatabaseModel {

    private Map<String, CreateTable> tables = new HashMap<>();

    private List<AddPrimaryKey> primaryKeys = new ArrayList<>();

    private List<AddForeignKey> foreignKeys = new ArrayList<>();


    public Collection<CreateTable> getTables() {
        return tables.values();
    }

    public void addTable(CreateTable table) {
        tables.put(table.getTableName(), table);
    }

    public CreateTable getTable(String tableName) {
        return tables.get(tableName);
    }


    /**
     * @return the primaryKeys
     */
    public List<AddPrimaryKey> getPrimaryKeys() {
        return primaryKeys;
    }


    /**
     * @param primaryKeys the primaryKeys to set
     */
    public void setPrimaryKeys(List<AddPrimaryKey> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }


    /**
     * @return the foreignKeys
     */
    public List<AddForeignKey> getForeignKeys() {
        return foreignKeys;
    }


    /**
     * @param foreignKeys the foreignKeys to set
     */
    public void setForeignKeys(List<AddForeignKey> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }



}
