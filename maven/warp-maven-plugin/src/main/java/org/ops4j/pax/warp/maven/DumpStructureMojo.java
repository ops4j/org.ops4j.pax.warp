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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.ops4j.pax.warp.core.changelog.impl.JaxbChangeLogWriter;
import org.ops4j.pax.warp.core.command.CommandRunner;
import org.ops4j.pax.warp.core.command.impl.CommandRunnerImpl;
import org.ops4j.pax.warp.core.update.impl.UpdateServiceImpl;
import org.ops4j.pax.warp.jaxb.WarpJaxbContext;


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

    private CommandRunner commandRunner;
    
    private void init() {
        // wire beans manually
        // TODO use Maven/Sisu dependency injection
        CommandRunnerImpl runner = new CommandRunnerImpl();
        UpdateServiceImpl updateService = new UpdateServiceImpl();
        runner.setUpdateService(updateService);
        WarpJaxbContext context = new WarpJaxbContext();
        context.init();
        JaxbChangeLogWriter changeLogWriter = new JaxbChangeLogWriter();
        changeLogWriter.setContext(context);
        runner.setChangeLogWriter(changeLogWriter);
        commandRunner = runner;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        init();
        try (OutputStream os = new FileOutputStream(changeLog)) {            
            commandRunner.dump(url, username, password, os);
        }
        catch (IOException exc) {
            throw new MojoExecutionException("error writing change log", exc);
        }
    }

}
