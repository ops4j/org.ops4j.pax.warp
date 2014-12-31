/*
 * Copyright 2014 EOS UPTRADE GmbH.
 *
 * This is proprietary software. All rights reserved. Unauthorized use is prohibited.
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
