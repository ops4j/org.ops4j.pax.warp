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
package org.ops4j.pax.warp.changelog.impl;

import java.util.function.Consumer;

import org.ops4j.pax.warp.jaxb.AddForeignKey;
import org.ops4j.pax.warp.jaxb.AddPrimaryKey;
import org.ops4j.pax.warp.jaxb.CreateTable;
import org.ops4j.pax.warp.jaxb.visitor.VisitorAction;


public class UpdateSqlGenerator extends AbstractSqlGenerator {


    public UpdateSqlGenerator(String dbms, Consumer<String> consumer) {
        super(dbms, consumer);
    }

    @Override
    public VisitorAction enter(CreateTable action) {
        return renderTemplate("createTable", action);
    }

    @Override
    public VisitorAction enter(AddPrimaryKey action) {
        return renderTemplate("addPrimaryKey", action);
    }

    @Override
    public VisitorAction enter(AddForeignKey action) {
        return renderTemplate("addForeignKey", action);
    }

}
