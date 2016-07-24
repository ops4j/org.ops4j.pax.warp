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

/**
 * Provides information about specific behaviour of a given database management system.
 *
 * @author Harald Wellmann
 *
 */
public interface DbmsProfile {

    /**
     * Gets the JDBC subprotocol for this DBMS.
     *
     * @return subprotocol
     */
    String getSubprotocol();

    /**
     * Does an auto-increment columns have to be a primary key column?
     *
     * @return true if a primary is automatically generated for an auto-increment column
     */
    default boolean getAutoIncrementIsPrimaryKey() {
        return false;
    }

    /**
     * Are table names case sensitive?
     *
     * @return true if table names are case-sensitive.
     */
    default boolean getTableNameIsCaseSensitive() {
        return false;
    }

    /**
     * Are schema names case sensitive?
     *
     * @return true if table names are case-sensitive.
     */
    default boolean getSchemaNameIsCaseSensitive() {
        return false;
    }

    /**
     * Is a schema equivalent to a catalog?
     *
     * @return false if the database supports real schemas
     */
    default boolean getSchemaIsCatalog() {
        return false;
    }

    /**
     * Is the index with this name an auxiliary index generated by the DBMS to support a constraint?
     *
     * @param indexName
     *            index name
     * @return true if index is generated
     */
    default boolean isGeneratedIndex(String indexName) {
        return false;
    }

    default String quoteIdentifier(String identifier) {
        StringBuilder builder = new StringBuilder();
        builder.append('"');
        builder.append(identifier);
        builder.append('"');
        return builder.toString();
    }
}
