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
package org.ops4.pax.warp.weld;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.ops4j.pax.warp.cli.CommandLineExecutor;

/**
 * Main class for stand-alone Pax Warp, based on Weld SE.
 *
 * @author Harald Wellmann
 *
 */
public class PaxWarpWeld {

    private PaxWarpWeld() {
        // hidden constructor
    }

    /**
     * Entry point for invoking Pax Warp from the command line.
     *
     * @param args
     *            command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("org.jboss.weld.se.archive.isolation", "false");
        System.setProperty("org.jboss.logging.provider", "slf4j");
        Weld weld = new Weld();
        WeldContainer container = weld.initialize();
        CommandLineExecutor warp = container.instance().select(CommandLineExecutor.class).get();
        warp.execute(args);
        weld.shutdown();
    }
}
