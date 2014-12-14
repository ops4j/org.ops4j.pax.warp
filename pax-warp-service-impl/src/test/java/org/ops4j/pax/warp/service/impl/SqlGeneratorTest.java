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
package org.ops4j.pax.warp.service.impl;

import java.util.Arrays;

import org.junit.Test;
import org.ops4j.pax.warp.changelog.impl.TableGenerator;
import org.ops4j.pax.warp.jaxb.AddPrimaryKey;
import org.ops4j.pax.warp.jaxb.Column;
import org.ops4j.pax.warp.jaxb.Constraints;
import org.ops4j.pax.warp.jaxb.CreateTable;
import org.ops4j.pax.warp.jaxb.SqlType;


public class SqlGeneratorTest {

    @Test
    public void shouldCreateTable() {
        CreateTable table = new CreateTable();
        table.setTableName("customer");
        Column firstName = new Column();
        firstName.setName("first_name");
        firstName.setType(SqlType.VARCHAR);
        firstName.setLength(255);
        Constraints notNull = new Constraints();
        notNull.setNullable(false);
        firstName.setConstraints(notNull);
        Column lastName = new Column();
        lastName.setName("last_name");
        lastName.setType(SqlType.VARCHAR);
        Column enabled = new Column();
        enabled.setName("enabled");
        enabled.setType(SqlType.BOOLEAN);
        table.getColumn().add(firstName);
        table.getColumn().add(lastName);
        table.getColumn().add(enabled);

        TableGenerator generator = new TableGenerator("mysql", System.out::println);
        table.accept(generator);
    }

    @Test
    public void shouldAddPrimaryKey() {
        AddPrimaryKey action = new AddPrimaryKey();
        action.setTableName("customer");
        action.setConstraintName("pk_customer");
        action.getColumn().addAll(Arrays.asList("first_name", "last_name"));

        TableGenerator generator = new TableGenerator("mysql", System.out::println);
        action.accept(generator);
    }
}
