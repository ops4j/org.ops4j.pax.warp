/*
 * Copyright 2014 Harald Wellmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 *
 * See the License for the specific language governing permissions and limitations under the
 * License.
 */
package org.ops4j.pax.warp.maven;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Analyzes the structure of the given database and writes a change log to the given file. The
 * change log also contains all data records of all tables.
 * <p>
 * Primary keys, foreign keys and indexes change sets follow the insert change sets to avoid
 * constraint violations while inserting data.
 *
 * @author hwellmann
 *
 */
@Mojo(name = "dump-all", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class DumpAllMojo extends AbstractWarpMojo {

    @Override
    public void execute() throws MojoExecutionException {
        try (OutputStream os = new FileOutputStream(changeLog)) {
            commandRunner.dumpAll(url, username, password, os);
        }
        catch (IOException exc) {
            throw new MojoExecutionException("error writing change log", exc);
        }
    }
}
