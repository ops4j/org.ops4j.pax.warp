/*
 * Copyright 2015 Harald Wellmann.
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Deletes all existing data in the given database, except from the excluded tables, and imports the
 * data from the given change log. Foreign key constraints are disabled during the import, so that
 * the order of data in the change log does not matter.
 *
 * @author hwellmann
 *
 */
@Mojo(name = "import-data", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class ImportDataMojo extends AbstractWarpMojo {

    /**
     * List of tables that will not be modified.
     */
    @Parameter(required = true, property = "warp.excludedTables")
    protected List<String> excludedTables;

    @Override
    public void execute() throws MojoExecutionException {

        try (InputStream is = new FileInputStream(changeLog)) {
            commandRunner.importData(url, username, password, is, excludedTables);
        }
        catch (IOException exc) {
            throw new MojoExecutionException("error reading change log", exc);
        }
    }
}
