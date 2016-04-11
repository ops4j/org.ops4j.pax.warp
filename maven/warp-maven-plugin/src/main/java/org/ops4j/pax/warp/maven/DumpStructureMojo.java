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
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Analyzes the structure of the given database and writes a change log corresponding to the
 * database structure to the given file. The change log does not contain any data records.
 *
 * @author hwellmann
 *
 */
@Mojo(name = "dump-structure", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class DumpStructureMojo extends AbstractWarpMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try (OutputStream os = new FileOutputStream(changeLog)) {
            commandRunner.dumpStructure(url, username, password, os);
        }
        catch (IOException exc) {
            throw new MojoExecutionException("error writing change log", exc);
        }
    }
}
