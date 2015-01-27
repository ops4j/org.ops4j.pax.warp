/*
 * Copyright 2015 Harald Wellmann.
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ops4j.pax.warp.core.trimou.JarClassPathTemplateLocator;
import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.engine.config.EngineConfigurationKey;
import org.trimou.engine.locator.ClassPathTemplateLocator;
import org.trimou.engine.locator.TemplateLocator;
import org.trimou.exception.MustacheException;
import org.trimou.util.Checker;

/**
 * @author Harald Wellmann
 *
 */
public class JarClassPathTemplateLocatorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void beforeClass() {
        URL url = JarClassPathTemplateLocatorTest.class.getClassLoader().getResource(
            "locator/file/foo.foo");
        assertThat(url, is(notNullValue()));
        assertThat(url.getProtocol(), is("jar"));
    }

    @Test
    public void classPathLocatorShouldFail() {
        thrown.expect(MustacheException.class);
        thrown.expectMessage("TEMPLATE_LOCATOR_INVALID_CONFIGURATION");

        new ClassPathTemplateLocator(1, "locator/file", "foo");
    }

    @Test
    public void testLocator() throws IOException {
        TemplateLocator locator = new JarClassPathTemplateLocator(1, "locator/file", "foo");

        // Just to init the locator
        MustacheEngineBuilder.newBuilder().addTemplateLocator(locator).build();

        assertEquals("{{foo}}", read(locator.locate("index")));
        assertEquals("bar", read(locator.locate("home")));
        assertEquals("foo", read(locator.locate("foo")));
        assertEquals("{{foo}}", read(locator.locate("sub/bar")));
        assertEquals("{{bar}}", read(locator.locate("sub/subsub/qux")));
    }

    @Test
    public void testLocatorNoSuffix() throws IOException {

        TemplateLocator locator = new JarClassPathTemplateLocator(1, "locator/file");

        // Just to init the locator
        MustacheEngineBuilder.newBuilder().addTemplateLocator(locator).build();

        assertEquals("{{foo}}", read(locator.locate("index.foo")));
        assertEquals("bar", read(locator.locate("home.foo")));
        assertEquals("foo", read(locator.locate("foo.foo")));
        assertEquals("<html/>", read(locator.locate("detail.html")));
        assertEquals("{{foo}}", read(locator.locate("sub/bar.foo")));
        assertEquals("{{bar}}", read(locator.locate("sub/subsub/qux.foo")));
    }

    @Test
    public void testEncoding() throws IOException {
        TemplateLocator locator = new JarClassPathTemplateLocator(1, "locator/file", "html");
        // Just to init the locator
        MustacheEngineBuilder.newBuilder()
            .setProperty(EngineConfigurationKey.DEFAULT_FILE_ENCODING, "windows-1250")
            .addTemplateLocator(locator).build();
        assertEquals("Hurá ěščřřžžýá!", read(locator.locate("encoding")));
    }

    @Test
    public void testLocatorClasspathNoRootPath() throws IOException {

        TemplateLocator locator = new JarClassPathTemplateLocator(1, null, "foo");

        // Just to init the locator
        MustacheEngineBuilder.newBuilder().addTemplateLocator(locator).build();

        Set<String> ids = locator.getAllIdentifiers();
        // No templates available
        assertEquals(0, ids.size());

        assertEquals("{{foo}}", read(locator.locate("locator/file/index")));
        assertEquals("bar", read(locator.locate("locator/file/home")));
        assertEquals("foo", read(locator.locate("locator/file/foo")));
        assertEquals("{{foo}}", read(locator.locate("locator/file/sub/bar")));
        assertEquals("{{bar}}", read(locator.locate("locator/file/sub/subsub/qux")));
        assertEquals("root", read(locator.locate("/oof")));
    }

    protected String read(Reader reader) throws IOException {
        Checker.checkArgumentNotNull(reader);
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder text = new StringBuilder();
        int character = bufferedReader.read();
        while (character != -1) {
            text.append((char) character);
            character = bufferedReader.read();
        }
        return text.toString();
    }
}
