/*
 * Copyright 2014 EOS UPTRADE GmbH.
 * 
 * This is proprietary software. All rights reserved. Unauthorized use is prohibited.
 */
package org.ops4j.pax.warp.core.command;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;

/**
 * @author hwellmann
 *
 */
public interface CommandRunner {

    void dump(String jdbcUrl, String username, String password, OutputStream os)
        throws SQLException, JAXBException;

    void dump(DataSource ds, OutputStream os) throws SQLException, JAXBException;

    void dump(Connection dbc, OutputStream os) throws SQLException, JAXBException;

    void dumpData(String jdbcUrl, String username, String password, OutputStream os)
        throws SQLException, JAXBException;

    void dumpData(Connection dbc, OutputStream os) throws SQLException, JAXBException;

    void update(String jdbcUrl, String username, String password, InputStream is)
        throws JAXBException, SQLException;

    void update(DataSource ds, InputStream is, String dbms) throws JAXBException,
        SQLException;

    void update(Connection dbc, InputStream is, String dbms) throws JAXBException,
        SQLException;

}
