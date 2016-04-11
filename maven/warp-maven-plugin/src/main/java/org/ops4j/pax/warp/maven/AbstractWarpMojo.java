/*
 * Copyright 2015 Harald Wellmann.
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
package org.ops4j.pax.warp.maven;

import java.io.File;

import javax.inject.Inject;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.ops4j.pax.warp.core.command.CommandRunner;


/**
 * @author hwellmann
 *
 */
public abstract class AbstractWarpMojo extends AbstractMojo {

    /**
     * JDBC URL of database.
     */
    @Parameter(required = true, property = "warp.url")
    protected String url;

    /**
     * Password for database connection.
     */
    @Parameter(property = "warp.password")
    protected String password;

    /**
     * User name for database connection.
     */
    @Parameter(property = "warp.username")
    protected String username;

    /**
     * Change log file.
     */
    @Parameter(required = true, property = "warp.changeLog")
    protected File changeLog;

    @Inject
    protected CommandRunner commandRunner;

}
