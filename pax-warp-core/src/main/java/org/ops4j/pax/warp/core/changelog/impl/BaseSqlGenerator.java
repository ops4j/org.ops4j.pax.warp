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
import org.ops4j.pax.warp.core.trimou.TrimmingLambda;
import org.ops4j.pax.warp.exc.WarpException;
import org.ops4j.pax.warp.jaxb.gen.ChangeSet;
import org.ops4j.pax.warp.jaxb.gen.visitor.BaseVisitor;
import org.ops4j.pax.warp.jaxb.gen.visitor.VisitorAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;
import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.engine.locator.ClassPathTemplateLocator;
import org.trimou.engine.resolver.CombinedIndexResolver;
import org.trimou.engine.resolver.MapResolver;
import org.trimou.engine.resolver.ReflectionResolver;
import org.trimou.engine.resolver.ThisResolver;
import org.trimou.handlebars.HelpersBuilder;

import com.google.common.collect.ImmutableMap;



public class BaseSqlGenerator extends BaseVisitor {

    protected Logger log = LoggerFactory.getLogger(BaseSqlGenerator.class);

    protected DbmsProfile dbms;
    protected Connection dbc;
    protected Consumer<PreparedStatement> consumer;
    protected Predicate<ChangeSet> changeSetFilter = x -> true;

    private MustacheEngine engine;

    protected BaseSqlGenerator(DbmsProfile dbms, Connection dbc,
        Consumer<PreparedStatement> consumer) {
        this.dbms = dbms;
        this.dbc = dbc;
        this.consumer = consumer;
        // generic SQL template
        ClassPathTemplateLocator genericLocator
            = new ClassPathTemplateLocator(100, "trimou/shared", "trimou");
        // DBMS specific templates, loaded with higher priority
        ClassPathTemplateLocator dbmsLocator
            = new ClassPathTemplateLocator(200, "trimou/" + dbms.getSubprotocol(), "trimou");
        engine = MustacheEngineBuilder.newBuilder()
            .addTemplateLocator(genericLocator)
            .addTemplateLocator(dbmsLocator)
            .registerHelpers(HelpersBuilder.builtin().addSwitch().build())
            .addGlobalData("trim", new TrimmingLambda())
            // manually add default extension to avoid META-INF/service classloader issues
            // when running under OSGi
            .omitServiceLoaderConfigurationExtensions()
            .addResolver(new ReflectionResolver())
            .addResolver(new ThisResolver()).addResolver(new MapResolver())
            .addResolver(new CombinedIndexResolver())
            .build();
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
        Mustache mustache = engine.getMustache(templateName);
        String result = mustache.render(ImmutableMap.of("action", action));
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
