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

import org.ops4j.pax.warp.core.dbms.DbmsProfile;
import org.ops4j.pax.warp.core.trimou.TemplateEngine;
import org.ops4j.pax.warp.exc.WarpException;
import org.ops4j.pax.warp.jaxb.gen.ChangeSet;
import org.ops4j.pax.warp.jaxb.gen.visitor.BaseVisitor;
import org.ops4j.pax.warp.jaxb.gen.visitor.VisitorAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class BaseSqlGenerator extends BaseVisitor {

    protected Logger log = LoggerFactory.getLogger(BaseSqlGenerator.class);

    protected DbmsProfile dbms;
    protected Connection dbc;
    protected Consumer<PreparedStatement> consumer;
    protected Predicate<ChangeSet> changeSetFilter = x -> true;
    protected TemplateEngine engine;

    protected BaseSqlGenerator(DbmsProfile dbms, Connection dbc,
        Consumer<PreparedStatement> consumer) {
        this.dbms = dbms;
        this.dbc = dbc;
        this.consumer = consumer;
        this.engine = new TemplateEngine(dbms.getSubprotocol());
    }

    protected VisitorAction produceStatement(String templateName, Object action) {
        String sql = engine.renderTemplate(templateName, action);
        runStatement(sql);

        return VisitorAction.CONTINUE;
    }

    protected void runStatement(String sql) {
        try (PreparedStatement st = dbc.prepareStatement(sql)) {
            consumer.accept(st);
        }
        catch (SQLException exc) {
            throw new WarpException(exc);
        }
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
