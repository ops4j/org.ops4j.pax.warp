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
package org.ops4j.pax.warp.itest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.linkBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.warp.itest.TestConfiguration.logbackBundles;
import static org.ops4j.pax.warp.itest.TestConfiguration.workspaceBundle;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.xml.bind.JAXBException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.Filter;
import org.ops4j.pax.warp.core.command.CommandRunner;
import org.osgi.service.jdbc.DataSourceFactory;


/**
 * @author Harald Wellmann
 *
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CommandRunnerTest {

    @Inject
    private CommandRunner commandRunner;

    @Inject
    @Filter("(osgi.jdbc.driver.name=h2)")
    private DataSourceFactory dsf;

    @Configuration
    public Option[] config() {
        return options(
            logbackBundles(),
            junitBundles(),

            linkBundle("org.ops4j.pax.jdbc.h2"),
            linkBundle("org.h2"),

            linkBundle("org.apache.felix.scr"),
            linkBundle("javax.enterprise.cdi-api"),
            linkBundle("javax.interceptor-api"),
            linkBundle("javax.el-api"),
            linkBundle("org.ops4j.pax.tipi.antlr.runtime"),
            linkBundle("org.ops4j.pax.tipi.stringtemplate"),

            workspaceBundle("pax-warp-core"),
            workspaceBundle("pax-warp-jaxb"));

    }

    @Test
    public void test01ShouldFindCommandRunner() {
        assertThat(commandRunner, is(notNullValue()));
    }

    @Test
    public void test02ShouldUpdateStructure() throws SQLException, JAXBException, IOException {
        Connection dbc = createConnection();
        InputStream is = getClass().getResourceAsStream("/changelogs/changelog1.xml");
        commandRunner.migrate(dbc, is, "h2");
        is.close();
        dbc.close();
    }

    @Test
    public void test03ShouldUpdateData() throws SQLException, JAXBException, IOException {
        Connection dbc = createConnection();
        InputStream is = getClass().getResourceAsStream("/changelogs/data1.xml");
        commandRunner.migrate(dbc, is, "h2");
        is.close();
        dbc.close();
    }

    @Test
    public void test04ShouldDumpData() throws SQLException, JAXBException, IOException {
        Connection dbc = createConnection();
        OutputStream os = new FileOutputStream("target/data1.xml");
        commandRunner.dumpData(dbc, os);
        os.close();
        dbc.close();
    }

    private Connection createConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, "mem:warp;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        props.setProperty(DataSourceFactory.JDBC_USER, "warp");
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, "warp");
        DataSource dataSource = dsf.createDataSource(props);
        assertThat(dataSource, is(notNullValue()));
        Connection dbc = dataSource.getConnection();
        assertThat(dbc, is(notNullValue()));
        return dbc;
    }
}
