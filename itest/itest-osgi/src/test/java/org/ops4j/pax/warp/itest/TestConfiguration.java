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
package org.ops4j.pax.warp.itest;

import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.linkBundle;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.when;

import java.io.File;
import java.util.ServiceLoader;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.launch.FrameworkFactory;

public class TestConfiguration {

    private static boolean consoleEnabled = Boolean.valueOf(System.getProperty("org.ops4j.pax.warp.console",
        "false"));

    public static Option logbackBundles() {
        return composite(
            when(consoleEnabled).useOptions(
                systemProperty("osgi.console").value("6666"),
                systemProperty("eclipse.consoleLog").value("true"),
                systemProperty("osgi.console.enable.builtin").value("true")),

            when(consoleEnabled && isFelix()).useOptions(
                mavenBundle("org.apache.felix", "org.apache.felix.gogo.command", "0.14.0"),
                mavenBundle("org.apache.felix", "org.apache.felix.gogo.runtime", "0.12.1"),
                mavenBundle("org.apache.felix", "org.apache.felix.gogo.shell", "0.10.0")),

            systemProperty("logback.configurationFile").value(
                "file:" + PathUtils.getBaseDir() + "/src/test/resources/logback.xml"),

            linkBundle("slf4j.api"),
            linkBundle("jcl.over.slf4j"),
            linkBundle("ch.qos.logback.core"),
            linkBundle("ch.qos.logback.classic"));
    }

    public static boolean isEquinox() {
        FrameworkFactory factory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
        return factory.getClass().getSimpleName().contains("Equinox");
    }

    public static boolean isFelix() {
        FrameworkFactory factory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
        return factory.getClass().getCanonicalName().contains("felix");
    }

    public static Option workspaceBundle(String artifactId) {
        String fileName = String.format("%s/../../%s/target/classes", PathUtils.getBaseDir(), artifactId);
        if (new File(fileName).exists()) {
            String url = "reference:file:" + fileName;
            return bundle(url);
        }
        else {
            return mavenBundle("org.ops4j.pax.warp", artifactId).versionAsInProject();
        }
    }
}
