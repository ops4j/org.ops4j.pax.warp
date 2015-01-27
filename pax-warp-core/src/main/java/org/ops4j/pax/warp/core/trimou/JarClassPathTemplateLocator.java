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
package org.ops4j.pax.warp.core.trimou;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trimou.engine.locator.PathTemplateLocator;
import org.trimou.exception.MustacheException;
import org.trimou.exception.MustacheProblem;
import org.trimou.util.Checker;
import org.trimou.util.Strings;

/**
 * @author Harald Wellmann
 *
 */
public class JarClassPathTemplateLocator extends PathTemplateLocator<String> {

    private static Logger log = LoggerFactory.getLogger(JarClassPathTemplateLocator.class);

    private ClassLoader classLoader;

    public JarClassPathTemplateLocator(int priority, String rootPath) {
        this(priority, rootPath, null, Thread.currentThread().getContextClassLoader());
    }

    public JarClassPathTemplateLocator(int priority, String rootPath, String suffix) {
        this(priority, rootPath, suffix, Thread.currentThread().getContextClassLoader());
    }

    public JarClassPathTemplateLocator(int priority, String rootPath, String suffix,
        ClassLoader classLoader) {
        super(priority, rootPath, suffix);
        Checker.checkArgumentNotNull(classLoader);
        this.classLoader = classLoader;
    }

    @Override
    public Reader locate(String templateId) {
        return locateRealPath(toRealPath(templateId));
    }

    /**
     * @param realPath
     * @return
     */
    private Reader locateRealPath(String realPath) {
        String name = getRootPath() != null ? getRootPath() + addSuffix(realPath)
            : addSuffix(realPath);
        InputStream in = classLoader.getResourceAsStream(name);
        if (in == null) {
            return null;
        }
        log.debug("Template located: {}", getRootPath() + realPath);
        try {
            return new InputStreamReader(in, getDefaultFileEncoding());
        }
        catch (UnsupportedEncodingException e) {
            throw new MustacheException(MustacheProblem.TEMPLATE_LOADING_ERROR, e);
        }
    }

    /**
     * Scanning and precompiling templates from the classpath is not currently supported.
     * Scanning is non-trivial as there is no API for listing all resources in a given directory,
     * and it is legal for JARs to have no directory entries at all.
     */
    @Override
    public Set<String> getAllIdentifiers() {
        return Collections.emptySet();
    }

    @Override
    protected String constructVirtualPath(String source) {

        String[] parts = StringUtils.split(
                StringUtils.substringAfter(source, getRootPath()),
                Strings.SLASH);

        StringBuilder name = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            name.append(parts[i]);
            if (i + 1 < parts.length) {
                name.append(getVirtualPathSeparator());
            }
        }
        return name.toString();
    }
}
