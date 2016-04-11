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
package org.ops4j.pax.warp.core.command;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.ops4j.pax.exam.junit.PaxExam;

/**
 * @author Harald Wellmann
 *
 */
@RunWith(PaxExam.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractCommandRunnerTest {

    @Inject
    private CommandRunner commandRunner;

    private void updateStructure(String jdbcUrl) throws JAXBException, SQLException, IOException {
        InputStream is = getClass().getResourceAsStream("/changelogs/changelog1.xml");
        Connection dbc = DriverManager.getConnection(jdbcUrl, "warp", "warp");
        commandRunner.migrate(dbc, is);
        dbc.close();
        is.close();
    }

    private void dumpDataOnly(String jdbcUrl) throws JAXBException, SQLException, IOException {
        Connection dbc = DriverManager.getConnection(jdbcUrl, "warp", "warp");
        OutputStream os = new FileOutputStream("target/dataOnly1.xml");
        commandRunner.dumpData(dbc, os);
        os.close();
        dbc.close();
    }

    private void dumpStructure(String jdbcUrl) throws JAXBException, SQLException, IOException {
        Connection dbc = DriverManager.getConnection(jdbcUrl, "warp", "warp");
        OutputStream os = new FileOutputStream("target/structure1.xml");
        commandRunner.dumpStructure(dbc, os);
        os.close();
        dbc.close();
    }

    private void reimportStructure(String jdbcUrl) throws JAXBException, SQLException, IOException {
        Connection dbc = DriverManager.getConnection(jdbcUrl, "warp", "warp");
        InputStream is = new FileInputStream("target/structure1.xml");
        commandRunner.migrate(dbc, is);
        is.close();
        dbc.close();
    }

    private void insertData(String jdbcUrl) throws JAXBException, SQLException, IOException {
        Connection dbc = DriverManager.getConnection(jdbcUrl, "warp", "warp");
        InputStream is = getClass().getResourceAsStream("/changelogs/data2.xml");
        commandRunner.importData(dbc, is);
        is.close();
        dbc.close();
    }

    private void reinsertData(String jdbcUrl) throws JAXBException, SQLException, IOException {
        Connection dbc = DriverManager.getConnection(jdbcUrl, "warp", "warp");
        InputStream is = new FileInputStream("target/dataOnly1.xml");
        commandRunner.importData(dbc, is);
        is.close();

        Statement st = dbc.createStatement();
        ResultSet rs = st.executeQuery("SELECT t FROM strings WHERE id = 'id4711'");
        assertThat(rs.next(), is(true));
        assertThat(rs.getString(1), is("This is a CLOB column."));
        rs.close();
        st.close();
        dbc.close();
    }

    private void runDropChangeSet(String jdbcUrl) throws JAXBException, SQLException, IOException {
        InputStream is = getClass().getResourceAsStream("/changelogs/changelog2.xml");
        Connection dbc = DriverManager.getConnection(jdbcUrl, "warp", "warp");
        commandRunner.migrate(dbc, is);
        dbc.close();
        is.close();
    }

    protected abstract String getJdbcUrl();

    protected abstract String getJdbcAdminUrl();

    protected abstract void dropAndCreateDatabase() throws SQLException;

    @Test
    public void test01ShouldUpdate() throws JAXBException, SQLException, IOException {
        dropAndCreateDatabase();
        updateStructure(getJdbcUrl());
        dumpStructure(getJdbcUrl());
    }

    @Test
    public void test02UpdateStructureShouldBeIdempotent()
        throws JAXBException, SQLException, IOException {
        updateStructure(getJdbcUrl());
    }

    @Test
    public void test03ShouldInsertData() throws JAXBException, SQLException, IOException {
        insertData(getJdbcUrl());
    }

    @Test
    public void test05ShouldDumpDataOnly() throws JAXBException, SQLException, IOException {
        dumpDataOnly(getJdbcUrl());
    }

    @Test
    public void test06ShouldInsertData() throws JAXBException, SQLException, IOException {
        reinsertData(getJdbcUrl());
    }

    @Test
    public void test07ShouldDropColumnAndIndex() throws JAXBException, SQLException, IOException {
        runDropChangeSet(getJdbcUrl());
    }

    @Test
    public void test08ShouldReimportStructure() throws JAXBException, SQLException, IOException {
        dropAndCreateDatabase();
        reimportStructure(getJdbcUrl());
    }
}
