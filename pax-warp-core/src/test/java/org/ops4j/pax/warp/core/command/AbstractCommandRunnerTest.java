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

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.warp.core.dbms.DbmsAdapter;


/**
 * @author Harald Wellmann
 *
 */
@RunWith(PaxExam.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractCommandRunnerTest {

    @Inject
    private CommandRunner commandRunner;

    private DbmsAdapter dbms;


    protected AbstractCommandRunnerTest(DbmsAdapter dbms) {
        this.dbms = dbms;
    }

    private void updateStructure() throws SQLException, IOException {
        InputStream inputStream = getClass().getResourceAsStream("/changelogs/changelog1.xml");
        migrateChangeSet(inputStream);
    }

    private void dumpDataOnly(String jdbcUrl) throws SQLException, IOException {
        Connection dbc = DriverManager.getConnection(jdbcUrl, "warp", "warp");
        OutputStream os = new FileOutputStream("target/dataOnly1.xml");
        commandRunner.dumpData(dbc, os);
        os.close();
        dbc.close();
    }

    private void dumpStructure(String jdbcUrl) throws  SQLException, IOException {
        Connection dbc = DriverManager.getConnection(jdbcUrl, "warp", "warp");
        OutputStream os = new FileOutputStream("target/structure1.xml");
        commandRunner.dumpStructure(dbc, os);
        os.close();
        dbc.close();
    }

    private void reimportStructure() throws  SQLException, IOException {
        InputStream inputStream = new FileInputStream("target/structure1.xml");
        migrateChangeSet(inputStream);
    }

    private void insertData1(String jdbcUrl) throws  SQLException, IOException {
        Connection dbc = DriverManager.getConnection(jdbcUrl, "warp", "warp");
        InputStream is = getClass().getResourceAsStream("/changelogs/data1.xml");
        commandRunner.importData(dbc, is);
        is.close();

        Statement st = dbc.createStatement();
        ResultSet rs = st.executeQuery("select id, v255 from strings where v4 is null");
        if (rs.next()) {
            assertThat(rs.getString(1), is("myid"));
            String v255 = rs.getString(2);
            assertThat(v255, anyOf(is(""), is(" ")));
        }
        else {
            fail("Expected non-empty result");
        }
        rs.close();

        st.close();
        dbc.close();
    }

    private void insertData2(String jdbcUrl) throws SQLException, IOException {
        Connection dbc = DriverManager.getConnection(jdbcUrl, "warp", "warp");
        InputStream is = getClass().getResourceAsStream("/changelogs/data2.xml");
        commandRunner.importData(dbc, is);
        is.close();
        dbc.close();
    }

    private void reinsertData(String jdbcUrl) throws SQLException, IOException {
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

    private void runDropChangeSet() throws SQLException, IOException {
        migrateChangeSet(getClass().getResourceAsStream("/changelogs/changelog2.xml"));
    }
    
    private void runRenameAndDropTableChangeSet() throws IOException, SQLException {
        migrateChangeSet(getClass().getResourceAsStream("/changelogs/changelog3.xml"));
    }

    private void runRunSqlChangeSet() throws IOException, SQLException {
        migrateChangeSet(getClass().getResourceAsStream("/changelogs/changelog4.xml"));
    }

    private void migrateChangeSet(InputStream inputStream) throws SQLException, IOException {
        Connection dbc = DriverManager.getConnection(getJdbcUrl(), "warp", "warp");
        commandRunner.migrate(dbc, inputStream);
        dbc.close();
        inputStream.close();
    }

    protected String getJdbcUrl() {
        return dbms.getJdbcUrl();
    }

    protected String getJdbcAdminUrl() {
        return dbms.getJdbcUrl();
    }

    protected void dropAndCreateDatabase() throws SQLException {
        dbms.dropAndCreateDatabase();
    }

    @Test
    public void test01ShouldUpdate() throws SQLException, IOException {
        dropAndCreateDatabase();
        updateStructure();
        dumpStructure(getJdbcUrl());
    }

    @Test
    public void test02UpdateStructureShouldBeIdempotent() throws  SQLException, IOException {
        updateStructure();
    }

    @Test
    public void test03ShouldInsertData() throws  SQLException, IOException {
        insertData1(getJdbcUrl());
    }

    @Test
    public void test04ShouldInsertData() throws  SQLException, IOException {
        insertData2(getJdbcUrl());
    }

    @Test
    public void test05ShouldDumpDataOnly() throws  SQLException, IOException {
        dumpDataOnly(getJdbcUrl());
    }

    @Test
    public void test06ShouldInsertData() throws  SQLException, IOException {
        reinsertData(getJdbcUrl());
    }

    @Test
    public void test07ShouldDropColumnAndIndex() throws SQLException, IOException {
        runDropChangeSet();
    }

    @Test
    public void test08ShouldReimportStructure() throws  SQLException, IOException {
        dropAndCreateDatabase();
        reimportStructure();
    }

    @Test
    public void test09ShouldRenameAndDropTable() throws IOException, SQLException {
        runRenameAndDropTableChangeSet();
    }

    @Test
    public void test10ShouldRunSqlInSelectedDbms() throws IOException, SQLException {
        runRunSqlChangeSet();
    }
}
