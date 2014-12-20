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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Consumer;

import org.ops4j.pax.warp.jaxb.visitor.BaseVisitor;
import org.ops4j.pax.warp.jaxb.visitor.VisitorAction;
import org.ops4j.pax.warp.util.Exceptions;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;



public class AbstractSqlGenerator extends BaseVisitor {


    protected Connection dbc;
    protected Consumer<PreparedStatement> consumer;
    protected STGroupFile templateGroup;

    public AbstractSqlGenerator(String dbms, Connection dbc, Consumer<PreparedStatement> consumer) {
        this.dbc = dbc;
        this.consumer = consumer;
        String templateGroupName = String.format("template/%s.stg", dbms);
        templateGroup = new STGroupFile(templateGroupName);
    }

    protected VisitorAction renderTemplate(String templateName, Object action) {
        ST template = templateGroup.getInstanceOf(templateName);
        template.add("action", action);
        String result = template.render();
        try (PreparedStatement st = dbc.prepareStatement(result)) {
            consumer.accept(st);
        }
        catch (SQLException exc) {
            throw Exceptions.unchecked(exc);
        }
        
        return VisitorAction.CONTINUE;
    }
}
