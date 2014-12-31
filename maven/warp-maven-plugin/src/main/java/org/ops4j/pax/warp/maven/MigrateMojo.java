/*
 * Copyright 2014 EOS UPTRADE GmbH.
 *
 * This is proprietary software. All rights reserved. Unauthorized use is prohibited.
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
            commandRunner.update(url, username, password, is);
        }
        catch (IOException exc) {
            throw new MojoExecutionException("error reading change log", exc);
        }
    }
}
