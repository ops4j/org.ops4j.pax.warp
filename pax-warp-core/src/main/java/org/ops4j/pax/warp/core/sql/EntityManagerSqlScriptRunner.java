/*
 * Copyright 2013 Harald Wellmann.
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
package org.ops4j.pax.warp.core.sql;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes an SQL script on a given EntityManager.
 * 
 * @author Harald Wellmann
 * 
 */
public class EntityManagerSqlScriptRunner extends AbstractSqlScriptRunner {

    private static Logger log = LoggerFactory.getLogger(EntityManagerSqlScriptRunner.class);

    private EntityManager em;

    /**
     * Creates a script runner for the given entity manager.
     * 
     * @param em
     *            entity manager
     */
    public EntityManagerSqlScriptRunner(EntityManager em) {
        this.em = em;
    }

    /**
     * Runs a single statement from the script.
     * 
     * @param sql
     *            SQL statement
     */
    protected void runStatement(String sql) {
        log.info("running SQL statement\n{};", sql);
        try {
            Query query = em.createNativeQuery(sql);
            query.executeUpdate();
        }
        catch (PersistenceException exc) {
            if (getTerminateOnError()) {
                throw exc;
            }
            log.error("error in SQL statement", exc);
        }
    }
}
