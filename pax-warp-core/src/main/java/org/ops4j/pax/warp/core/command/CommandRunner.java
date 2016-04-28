/*
 * Copyright 2014 EOS UPTRADE GmbH.
 *
 * This is proprietary software. All rights reserved. Unauthorized use is prohibited.
 */
package org.ops4j.pax.warp.core.command;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

/**
 * Interface for embedding Pax Warp into client applications via dependency injection.
 *
 *
 * @author Harald Wellmann
 *
 */
public interface CommandRunner {

    /**
     * Analyzes the structure of the database with the given URL and writes a change log
     * corresponding to the database structure to the given output stream. The change log does not
     * contain any data records.
     *
     * @param jdbcUrl
     *            JDBC URL of database
     * @param username
     *            database username
     * @param password
     *            database password
     * @param os
     *            output stream for change log
     */
    void dumpStructure(String jdbcUrl, String username, String password, OutputStream os);

    /**
     * Analyzes the structure of the database with the given data source and writes a change log
     * corresponding to the database structure to the given output stream. The change log does not
     * contain any data records.
     *
     * @param ds
     *            JDBC data source
     * @param os
     *            output stream for change log
     */
    void dumpStructure(DataSource ds, OutputStream os);

    /**
     * Analyzes the structure of the database with the given connection and writes a change log
     * corresponding to the database structure to the given output stream. The change log does not
     * contain any data records.
     *
     * @param dbc
     *            JDBC database connection
     * @param os
     *            output stream for change log
     */
    void dumpStructure(Connection dbc, OutputStream os);

    /**
     * Analyzes the structure of the tables of the given schema of the database with the given
     * connection and writes a change log corresponding to the database structure to the given
     * output stream. The change log does not contain any data records.
     *
     * @param dbc
     *            JDBC database connection
     * @param os
     *            output stream for change log
     * @param schema
     *            database schema
     */
    void dumpStructure(Connection dbc, OutputStream os, String schema);

    /**
     * Retrieves all data from the database with the given URL and writes a change log with these
     * data to the given output stream. The change log does not contain any structural information
     * like constraints or indexes.
     *
     * @param jdbcUrl
     *            JDBC URL of database
     * @param username
     *            database username
     * @param password
     *            database password
     * @param os
     *            output stream for change log
     */
    void dumpData(String jdbcUrl, String username, String password, OutputStream os);

    /**
     * Retrieves all data from the database with the given connection and writes a change log with
     * these data to the given output stream. The change log does not contain any structural
     * information like constraints or indexes.
     *
     * @param dbc
     *            JDBC database connection
     * @param os
     *            output stream for change log
     */
    void dumpData(Connection dbc, OutputStream os);

    /**
     * Retrieves all data from the tables of the given schema of the database with the given
     * connection and writes a change log with these data to the given output stream. The change log
     * does not contain any structural information like constraints or indexes.
     *
     * @param dbc
     *            JDBC database connection
     * @param os
     *            output stream for change log
     * @param schema
     *            database schema
     */
    void dumpData(Connection dbc, OutputStream os, String schema);

    /**
     * Retrieves all data from the database with the given data source and writes a change log with
     * these data to the given output stream. The change log does not contain any structural
     * information like constraints or indexes.
     *
     * @param ds
     *            JDBC data source
     * @param os
     *            output stream for change log
     */
    void dumpData(DataSource ds, OutputStream os);

    /**
     * Migrates the database with the given URL by applying the change log from the given input
     * stream.
     *
     * @param jdbcUrl
     *            JDBC URL of database
     * @param username
     *            database username
     * @param password
     *            database password
     * @param is
     *            input stream for change log
     */
    void migrate(String jdbcUrl, String username, String password, InputStream is);

    /**
     * Migrates the database with the given data source by applying the change log from the given
     * input stream.
     *
     * @param ds
     *            JDBC data source
     * @param is
     *            input stream for change log
     */
    void migrate(DataSource ds, InputStream is);

    /**
     * Migrates the database with the given connection by applying the change log from the given
     * input stream.
     *
     * @param dbc
     *            JDBC database connection
     * @param is
     *            input stream for change log
     */
    void migrate(Connection dbc, InputStream is);

    /**
     * Migrates the database with the given connection by applying the change log from the given
     * input stream.
     *
     * @param dbc
     *            JDBC database connection
     * @param is
     *            input stream for change log
     * @param schema
     *            database schema which will be created if needed and will be set on the
     *            given connection for the duration of this command
     */
    void migrate(Connection dbc, InputStream is, String schema);

    /**
     * Deletes all existing data in the given database, except from the excluded tables, and imports
     * the data from the given change log stream. Foreign key constraints are disabled during the
     * import, so that the order of data in the change log does not matter.
     *
     * @param jdbcUrl
     *            JDBC URL of database
     * @param username
     *            database username
     * @param password
     *            database password
     * @param is
     *            input stream for change log
     * @param excludedTables
     *            list of tables which will not be deleted
     */
    void importData(String jdbcUrl, String username, String password, InputStream is,
        List<String> excludedTables);

    /**
     * Deletes all existing data in the given database, and imports the data from the given change
     * log stream. Foreign key constraints are disabled during the import, so that the order of data
     * in the change log does not matter.
     *
     * @param jdbcUrl
     *            JDBC URL of database
     * @param username
     *            database username
     * @param password
     *            database password
     * @param is
     *            input stream for change log
     */
    void importData(String jdbcUrl, String username, String password, InputStream is);

    /**
     * Deletes all existing data in the given database and imports the data from the given change
     * log stream. Foreign key constraints are disabled during the import, so that the order of data
     * in the change log does not matter.
     *
     * @param dbc
     *            JDBC database connection
     * @param is
     *            input stream for change log
     */
    void importData(Connection dbc, InputStream is);

    /**
     * Deletes all existing data in the given database and imports the data from the given change
     * log stream. Foreign key constraints are disabled during the import, so that the order of data
     * in the change log does not matter.
     *
     * @param dbc
     *            JDBC database connection
     * @param is
     *            input stream for change log
     * @param schema
     *            database schema which must exist and will be set on the given connection
     *            for the duration of this command
     */
    void importData(Connection dbc, InputStream is, String schema);

    /**
     * Deletes all existing data in the given database, except from the excluded tables, and imports
     * the data from the given change log stream. Foreign key constraints are disabled during the
     * import, so that the order of data in the change log does not matter.
     *
     * @param dbc
     *            JDBC database connection
     * @param is
     *            input stream for change log
     * @param excludedTables
     *            list of tables which will not be deleted
     */
    void importData(Connection dbc, InputStream is, List<String> excludedTables);

    /**
     * Deletes all existing data in the given database and imports the data from the given change
     * log stream. Foreign key constraints are disabled during the import, so that the order of data
     * in the change log does not matter.
     *
     * @param ds
     *            JDBC data source
     * @param is
     *            input stream for change log
     */
    void importData(DataSource ds, InputStream is);

    /**
     * Deletes all existing data in the given database, except from the excluded tables, and imports
     * the data from the given change log stream. Foreign key constraints are disabled during the
     * import, so that the order of data in the change log does not matter.
     *
     * @param ds
     *            JDBC data source
     * @param is
     *            input stream for change log
     * @param excludedTables
     *            list of tables which will not be deleted
     */
    void importData(DataSource ds, InputStream is, List<String> excludedTables);
}
