/*
 * Copyright 2014 EOS UPTRADE GmbH.
 *
 * This is proprietary software. All rights reserved. Unauthorized use is prohibited.
 */
package org.ops4j.pax.warp.maven;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.inject.Inject;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.ops4j.pax.warp.core.command.CommandRunner;


/**
 * @author hwellmann
 *
 */
@Mojo(name = "dump-structure", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class DumpStructureMojo extends AbstractMojo {

    @Parameter(required = true)
    protected String url;

    @Parameter
    protected String password;

    @Parameter
    protected String username;

    @Parameter(required = true)
    protected File changeLog;

    @Inject
    private CommandRunner commandRunner;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try (OutputStream os = new FileOutputStream(changeLog)) {
            commandRunner.dump(url, username, password, os);
        }
        catch (IOException exc) {
            throw new MojoExecutionException("error writing change log", exc);
        }
    }
}
