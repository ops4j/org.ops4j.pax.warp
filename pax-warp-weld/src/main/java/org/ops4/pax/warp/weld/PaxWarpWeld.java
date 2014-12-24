package org.ops4.pax.warp.weld;
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

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.ops4j.pax.warp.cli.PaxWarp;




/**
 * @author Harald Wellmann
 *
 */
public class PaxWarpWeld {

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.setProperty("org.jboss.weld.se.archive.isolation", "false");
        System.setProperty("org.jboss.logging.provider", "slf4j");
        Weld weld = new Weld();
        WeldContainer container = weld.initialize();
        PaxWarp warp = container.instance().select(PaxWarp.class).get();
        warp.execute(args);
        weld.shutdown();
    }
}
