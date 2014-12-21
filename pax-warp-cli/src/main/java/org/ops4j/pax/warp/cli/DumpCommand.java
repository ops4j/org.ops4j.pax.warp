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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

import javax.xml.bind.JAXBException;

import org.ops4j.pax.warp.command.CommandRunner;
import org.ops4j.pax.warp.command.impl.CommandRunnerImpl;
import org.ops4j.pax.warp.util.Exceptions;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * @author Harald Wellmann
 *
 */
@Parameters(commandDescription = "dumps a database")
public class DumpCommand implements Runnable {

    @Parameter(names = "--url", description = "JDBC URL", required = true)
    private String url;

    @Parameter(names = "--username", description = "JDBC username")
    private String username;

    @Parameter(names = "--password", description = "JDBC password")
    private String password;

    @Parameter(names = "--output", description = "output file path")
    private String output;

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url
     *            the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username
     *            the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void run() {
        CommandRunner commandRunner = new CommandRunnerImpl();
        try {
            if (output == null) {
                commandRunner.dump(url, username, password, System.out);
            }
            else {
                OutputStream os = new FileOutputStream(output);
                commandRunner.dump(url, username, password, os);
                os.close();
            }
        }
        catch (IOException | SQLException | JAXBException exc) {
            throw Exceptions.unchecked(exc);
        }
    }

}
