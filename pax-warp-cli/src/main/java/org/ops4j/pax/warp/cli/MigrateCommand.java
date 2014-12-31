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
package org.ops4j.pax.warp.cli;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.ops4j.pax.warp.core.command.CommandRunner;
import org.ops4j.pax.warp.exc.WarpException;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * @author Harald Wellmann
 *
 */
@Dependent
@Parameters(commandDescription = "migrates a database, applying change sets from a change log")
public class MigrateCommand implements Runnable {

    @Inject
    private CommandRunner commandRunner;

    @Parameter(names = "--url", description = "JDBC URL")
    private String url;

    @Parameter(names = "--username", description = "JDBC username")
    private String username;

    @Parameter(names = "--password", description = "JDBC password")
    private String password;

    @Parameter(names = "--change-log", description = "change log file")
    private String changeLog;

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return the change log
     */
    public String getChangeLog() {
        return changeLog;
    }

    @Override
    public void run() {
        try {
            InputStream is = new FileInputStream(changeLog);
            commandRunner.migrate(url, username, password, is);
            is.close();
        }
        catch (IOException exc) {
            throw new WarpException(exc);
        }
    }
}
