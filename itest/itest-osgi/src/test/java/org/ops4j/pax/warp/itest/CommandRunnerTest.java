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
package org.ops4j.pax.warp.itest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.linkBundle;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.warp.itest.TestConfiguration.logbackBundles;
import static org.ops4j.pax.warp.itest.TestConfiguration.workspaceBundle;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.warp.core.command.CommandRunner;


/**
 * @author Harald Wellmann
 *
 */
@RunWith(PaxExam.class)
public class CommandRunnerTest {

    @Inject
    private CommandRunner commandRunner;

    @Configuration
    public Option[] config() {
        return options(
            logbackBundles(),
            junitBundles(),

            linkBundle("org.apache.felix.scr"),
            linkBundle("javax.enterprise.cdi-api"),
            linkBundle("javax.interceptor-api"),
            linkBundle("javax.el-api"),
            mavenBundle("org.ops4j.pax.tipi", "org.ops4j.pax.tipi.antlr.runtime", "3.5.2.1-SNAPSHOT"),
            mavenBundle("org.ops4j.pax.tipi", "org.ops4j.pax.tipi.stringtemplate", "4.0.8.1-SNAPSHOT"),

            workspaceBundle("pax-warp-core"),
            workspaceBundle("pax-warp-jaxb"));

    }

    @Test
    public void shouldFindCommandRunner() {
        assertThat(commandRunner, is(notNullValue()));
    }

}
