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
package org.ops4j.pax.warp.maven;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;


/**
 * @author hwellmann
 *
 */
@Mojo(name = "migrate", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class MigrateMojo extends AbstractWarpMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try (InputStream is = new FileInputStream(changeLog)) {
            commandRunner.migrate(url, username, password, is);
        }
        catch (IOException exc) {
            throw new MojoExecutionException("error reading change log", exc);
        }
    }
}
