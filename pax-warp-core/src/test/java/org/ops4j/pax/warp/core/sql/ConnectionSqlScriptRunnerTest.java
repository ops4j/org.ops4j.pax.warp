package org.ops4j.pax.warp.core.sql;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests {@link ConnectionSqlScriptRunner}.
 * 
 * @author Harald Wellmann
 */
public class ConnectionSqlScriptRunnerTest {
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Connection dbc;

    @Before
    public void before() throws SQLException {
        String url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
        dbc = DriverManager.getConnection(url);
    }

    @After
    public void after() throws SQLException {
        dbc.close();
    }

    @Test
    public void shouldRunScript() throws IOException, SQLException {
        AbstractSqlScriptRunner runner = new ConnectionSqlScriptRunner(dbc);
        try (Reader reader = new FileReader("src/test/resources/sql/test1.sql")) {
            runner.executeScript(reader);
        }

        verifyData();
    }

    /**
     * @throws SQLException
     */
    private void verifyData() throws SQLException {
        Statement st = dbc.createStatement();
        ResultSet rs = st.executeQuery("select count(*) from foo");
        if (rs.next()) {
            Long count = rs.getLong(1);
            assertThat(count, is(5L));
        }
        else {
            fail("result set is empty");
        }
        rs.close();
        st.close();
    }

    @Test
    public void shouldRunScriptResource() throws IOException, SQLException {
        AbstractSqlScriptRunner runner = new ConnectionSqlScriptRunner(dbc);
        runner.executeScript(getClass(), "test3.sql");

        verifyData();
    }

    @Test
    public void shouldRunScriptResourceWithVersionAndDialect() throws IOException, SQLException {
        AbstractSqlScriptRunner runner = new ConnectionSqlScriptRunner(dbc);
        runner.setVersion("2.0");
        runner.setDialect("postgresql");
        runner.executeScript(getClass(), "test4.sql");

        verifyData();
    }

    @Test
    public void shouldNotFindScriptWhenDialectNotSet() throws IOException, SQLException {
        AbstractSqlScriptRunner runner = new ConnectionSqlScriptRunner(dbc);
        runner.setVersion("2.0");
        
        thrown.expect(IllegalArgumentException.class);
        runner.executeScript(getClass(), "test4.sql");
    }

    @Test
    public void shouldNotFindScriptWhenVersionNotSet() throws IOException, SQLException {
        AbstractSqlScriptRunner runner = new ConnectionSqlScriptRunner(dbc);
        runner.setDialect("postgresql");
        
        thrown.expect(IllegalArgumentException.class);
        runner.executeScript(getClass(), "test4.sql");
    }
}
