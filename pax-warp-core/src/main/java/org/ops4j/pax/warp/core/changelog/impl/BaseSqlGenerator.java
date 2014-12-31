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
package org.ops4j.pax.warp.core.changelog.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.ops4j.pax.warp.exc.WarpException;
import org.ops4j.pax.warp.jaxb.gen.ChangeSet;
import org.ops4j.pax.warp.jaxb.gen.visitor.BaseVisitor;
import org.ops4j.pax.warp.jaxb.gen.visitor.VisitorAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;



public class BaseSqlGenerator extends BaseVisitor {

    protected Logger log = LoggerFactory.getLogger(BaseSqlGenerator.class);

    protected String dbms;
    protected Connection dbc;
    protected Consumer<PreparedStatement> consumer;
    protected STGroupFile templateGroup;
    protected Predicate<ChangeSet> changeSetFilter = x -> true;

    protected BaseSqlGenerator(String dbms, Connection dbc, Consumer<PreparedStatement> consumer) {
        this.dbms = dbms;
        this.dbc = dbc;
        this.consumer = consumer;
        String templateGroupName = String.format("template/%s.stg", dbms);
        templateGroup = new STGroupFile(templateGroupName);
    }

    protected VisitorAction produceStatement(String templateName, Object action) {
        String sql = renderTemplate(templateName, action);
        try (PreparedStatement st = dbc.prepareStatement(sql)) {
            consumer.accept(st);
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }

        return VisitorAction.CONTINUE;
    }

    protected String renderTemplate(String templateName, Object action) {
        ST template = templateGroup.getInstanceOf(templateName);
        template.add("action", action);

        String result = template.render();
        log.info(result);
        return result;
    }


    /**
     * @return the changeSetFilter
     */
    public Predicate<ChangeSet> getChangeSetFilter() {
        return changeSetFilter;
    }


    /**
     * @param changeSetFilter the changeSetFilter to set
     */
    public void setChangeSetFilter(Predicate<ChangeSet> changeSetFilter) {
        this.changeSetFilter = changeSetFilter;
    }


}
