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
package org.ops4j.pax.warp.core.service.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.warp.core.trimou.JarClassPathTemplateLocator;
import org.ops4j.pax.warp.jaxb.gen.AddForeignKey;
import org.ops4j.pax.warp.jaxb.gen.AddPrimaryKey;
import org.ops4j.pax.warp.jaxb.gen.Column;
import org.ops4j.pax.warp.jaxb.gen.ColumnPair;
import org.ops4j.pax.warp.jaxb.gen.ColumnReference;
import org.ops4j.pax.warp.jaxb.gen.ColumnValue;
import org.ops4j.pax.warp.jaxb.gen.CreateIndex;
import org.ops4j.pax.warp.jaxb.gen.CreateTable;
import org.ops4j.pax.warp.jaxb.gen.DropPrimaryKey;
import org.ops4j.pax.warp.jaxb.gen.Insert;
import org.ops4j.pax.warp.jaxb.gen.SqlType;
import org.ops4j.pax.warp.jaxb.gen.TableReference;
import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;
import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.handlebars.HelpersBuilder;
import org.trimou.lambda.InputProcessingLambda;
import org.trimou.lambda.Lambda;

import com.google.common.collect.ImmutableMap;

public class TrimouTest {

    private MustacheEngine engine;

    @Before
    public void before() {
        Lambda trim = new InputProcessingLambda() {

            @Override
            public boolean isReturnValueInterpolated() {
                return false;
            }

            @Override
            public String invoke(String text) {
                return text.trim();
            }
        };
        engine = MustacheEngineBuilder.newBuilder()
            .addTemplateLocator(new JarClassPathTemplateLocator(100, "trimou/shared", "trimou"))
            .addTemplateLocator(new JarClassPathTemplateLocator(200, "trimou/h2", "trimou"))
            .registerHelpers(HelpersBuilder.empty().addSwitch().build())
            .addGlobalData("trim", trim)
            .build();

    }

    @Test
    public void shouldRenderDropPrimaryKey() {
        Mustache mustache = engine.getMustache("dropPrimaryKey");
        DropPrimaryKey action = new DropPrimaryKey();
        action.setTableName("foo");
        System.out.println(mustache.render(ImmutableMap.of("action", action)));
    }

    @Test
    public void shouldRenderAddPrimaryKey() {
        Mustache mustache = engine.getMustache("addPrimaryKey");
        AddPrimaryKey action = new AddPrimaryKey();
        action.setTableName("foo");
        action.getColumn().add("c1");
        action.getColumn().add("c2");
        System.out.println(mustache.render(ImmutableMap.of("action", action)));
    }

    @Test
    public void shouldRenderCreateTable() {
        Mustache mustache = engine.getMustache("createTable");
        CreateTable action = new CreateTable();
        action.setTableName("foo");
        Column c1 = new Column();
        c1.setName("c1");
        c1.setType(SqlType.INT_32);
        Column c2 = new Column();
        c2.setName("c2");
        c2.setType(SqlType.INT_64);
        action.getColumn().add(c1);
        action.getColumn().add(c2);
        System.out.println(mustache.render(ImmutableMap.of("action", action)));
    }

    @Test
    public void shouldRenderCreateTableWithOverride() {
        Mustache mustache = engine.getMustache("createTable");
        CreateTable action = new CreateTable();
        action.setTableName("foo");
        Column c1 = new Column();
        c1.setName("c1");
        c1.setType(SqlType.INT_32);
        c1.setNullable(false);
        c1.setAutoIncrement(true);
        Column c2 = new Column();
        c2.setName("c2");
        c2.setType(SqlType.INT_64);
        c2.setNullable(false);
        Column c3 = new Column();
        c3.setName("c3");
        c3.setType(SqlType.VARCHAR);
        c3.setLength(10);
        Column c4 = new Column();
        c4.setName("c4");
        c4.setType(SqlType.DECIMAL);
        c4.setPrecision(5);
        c4.setScale(2);
        action.getColumn().add(c1);
        action.getColumn().add(c2);
        action.getColumn().add(c3);
        action.getColumn().add(c4);
        System.out.println(mustache.render(ImmutableMap.of("action", action)));
    }

    @Test
    public void shouldRenderAddForeignKey() {
        Mustache mustache = engine.getMustache("addForeignKey");
        AddForeignKey action = new AddForeignKey();
        action.setConstraintName("fk_4711");
        TableReference baseTable = new TableReference();
        baseTable.setTableName("order");
        TableReference refTable = new TableReference();
        refTable.setTableName("customer");
        action.setBaseTable(baseTable);
        action.setReferencedTable(refTable);
        ColumnPair cp1 = new ColumnPair();
        ColumnReference base1 = new ColumnReference();
        base1.setColumnName("customer_id");
        ColumnReference ref1 = new ColumnReference();
        ref1.setColumnName("id");
        cp1.setBase(base1);
        cp1.setReferenced(ref1);
        action.getColumnPair().add(cp1);

        ColumnPair cp2 = new ColumnPair();
        ColumnReference base2 = new ColumnReference();
        base2.setColumnName("product_id");
        ColumnReference ref2 = new ColumnReference();
        ref2.setColumnName("prod_id");
        cp2.setBase(base2);
        cp2.setReferenced(ref2);
        action.getColumnPair().add(cp2);
        System.out.println(mustache.render(ImmutableMap.of("action", action)));
    }

    @Test
    public void shouldRenderCreateIndex() {
        Mustache mustache = engine.getMustache("createIndex");
        CreateIndex action = new CreateIndex();
        action.setUnique(true);
        action.setTableName("foo");
        action.setIndexName("i_order");
        Column c1 = new Column();
        c1.setName("c1");
        Column c2 = new Column();
        c2.setName("c2");
        action.getColumn().add(c1);
        action.getColumn().add(c2);
        System.out.println(mustache.render(ImmutableMap.of("action", action)));

    }

    @Test
    public void shouldRenderInsert() {
        Mustache mustache = engine.getMustache("insert");
        Insert action = new Insert();
        action.setTableName("foo");
        ColumnValue cv1 = new ColumnValue();
        cv1.setName("c1");
        cv1.setValue("17");

        ColumnValue cv2 = new ColumnValue();
        cv2.setName("c2");
        cv2.setValue("abc");

        action.getColumn().add(cv1);
        action.getColumn().add(cv2);
        System.out.println(mustache.render(ImmutableMap.of("action", action)));

    }

    @Test
    public void shouldResolveJarUrl() throws URISyntaxException, IOException {
        URL resource = org.h2.Driver.class.getResource("Driver.class");
        System.out.println(resource);
        URI parent = resource.toURI().resolve(".");
        System.out.println(parent);

        URLConnection connection = resource.openConnection();
        assertThat(connection, instanceOf(JarURLConnection.class));
        JarURLConnection jarConnection = (JarURLConnection) connection;
        assertThat(jarConnection.getEntryName(), is("org/h2/Driver.class"));
    }


}
