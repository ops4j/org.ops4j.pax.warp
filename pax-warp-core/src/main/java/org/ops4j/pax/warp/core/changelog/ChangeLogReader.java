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
package org.ops4j.pax.warp.core.changelog;

import java.io.Reader;

import org.ops4j.pax.warp.jaxb.gen.ChangeLog;

/**
 * Unmarshals change logs from XML.
 *
 * @author Harald Wellmann
 *
 */
public interface ChangeLogReader {

    /**
     * Unmarshals a change log from XML.
     *
     * @param reader
     *            reader for XML data
     * @return change log model object
     */
    ChangeLog parse(Reader reader);
}
