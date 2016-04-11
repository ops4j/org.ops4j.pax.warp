/*
 * Copyright 2016 Harald Wellmann.
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
package org.ops4j.pax.warp.core.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
public abstract class AbstractSchemaHandlerTest {

    protected abstract String getSubprotocol();

    protected abstract String getJdbcUrl();

    protected abstract String getJdbcAdminUrl();

    protected abstract void dropAndCreateDatabase() throws SQLException;


    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getJdbcUrl(), "warp", "warp");
    }

    @Test
    public void test01ShouldGetDefaultSchema() throws SQLException {
        dropAndCreateDatabase();
        SchemaHandler schemaHandler = new SchemaHandler(getSubprotocol());
        Connection dbc = getConnection();
        assertThat(schemaHandler.getCurrentSchema(dbc).toLowerCase(), is("public"));
    }

    @Test
    public void test02ShouldCreateSchema() throws SQLException {
        SchemaHandler schemaHandler = new SchemaHandler(getSubprotocol());
        Connection dbc = getConnection();
        schemaHandler.createAndSetSchema(dbc, "foo");
        assertThat(schemaHandler.getCurrentSchema(dbc).toLowerCase(), is("foo"));
    }

    @Test
    public void test03ShouldCreateSchemaIdempotent() throws SQLException {
        SchemaHandler schemaHandler = new SchemaHandler(getSubprotocol());
        Connection dbc = getConnection();
        schemaHandler.createAndSetSchema(dbc, "foo");
        assertThat(schemaHandler.getCurrentSchema(dbc).toLowerCase(), is("foo"));
    }

    @Test
    public void test04ShouldSetSchema() throws SQLException {
        SchemaHandler schemaHandler = new SchemaHandler(getSubprotocol());
        Connection dbc = getConnection();
        assertThat(schemaHandler.getCurrentSchema(dbc).toLowerCase(), is("public"));
        schemaHandler.setCurrentSchema(dbc, "foo");
        assertThat(schemaHandler.getCurrentSchema(dbc).toLowerCase(), is("foo"));
    }
}
