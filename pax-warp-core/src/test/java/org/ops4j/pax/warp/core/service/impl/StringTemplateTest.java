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

import org.junit.Test;
import org.ops4j.pax.warp.jaxb.gen.Column;
import org.ops4j.pax.warp.jaxb.gen.Constraints;
import org.ops4j.pax.warp.jaxb.gen.CreateTable;
import org.ops4j.pax.warp.jaxb.gen.SqlType;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;


public class StringTemplateTest {

    public static class Person {
        private String firstName;
        private String lastName;


        public Person() {
            // empty
        }

        public Person(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }



        /**
         * @return the firstName
         */
        public String getFirstName() {
            return firstName;
        }

        /**
         * @param firstName the firstName to set
         */
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        /**
         * @return the lastName
         */
        public String getLastName() {
            return lastName;
        }

        /**
         * @param lastName the lastName to set
         */
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }


    }

    @Test
    public void render1() {
        ST hello = new ST("Hello, <name>");
        hello.add("name", "World");
        System.out.println(hello.render());
    }

    @Test
    public void renderBean() {
        ST hello = new ST("Hello, <p.firstName> <p.lastName>!");
        hello.add("p", new Person("Donald", "Duck"));
        System.out.println(hello.render());
    }

    @Test
    public void renderFromGroup() {
        STGroupFile stg = new STGroupFile("templates/CreateTableGroup.stg");
        ST template = stg.getInstanceOf("createTable");
        CreateTable table = new CreateTable();
        table.setTableName("customer");
        Column firstName = new Column();
        firstName.setName("first_name");
        firstName.setType(SqlType.VARCHAR);
        Constraints notNull = new Constraints();
        notNull.setNullable(false);
        firstName.getConstraints();
        Column lastName = new Column();
        lastName.setName("last_name");
        lastName.setType(SqlType.VARCHAR);
        table.getColumn().add(firstName);
        table.getColumn().add(lastName);
        template.add("c", table);
        System.out.println(template.render());
    }



}
