/*
 * Copyright 2014 EOS UPTRADE GmbH.
 *
 * This is proprietary software. All rights reserved. Unauthorized use is prohibited.
 */
package org.ops4j.pax.warp.maven;

import java.io.File;

import javax.inject.Inject;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.ops4j.pax.warp.core.command.CommandRunner;


/**
 * @author hwellmann
 *
 */
public abstract class AbstractWarpMojo extends AbstractMojo {

    @Parameter(required = true)
    protected String url;

    @Parameter
    protected String password;

    @Parameter
    protected String username;

    @Parameter(required = true)
    protected File changeLog;

    @Inject
    protected CommandRunner commandRunner;

}
