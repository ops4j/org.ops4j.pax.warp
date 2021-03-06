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

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;

import org.ops4j.pax.warp.jaxb.gen.ChangeLog;

/**
 * Marshals change logs to XML.
 *
 * @author Harald Wellmann
 *
 */
public interface ChangeLogWriter {

    /**
     * Marshals a change log model as XML to the given writer.
     *
     * @param changeLog
     *            change log model
     * @param writer
     *            writer for XML document
     */
    void write(ChangeLog changeLog, Writer writer);

    /**
     * Marshals a change log model as XML to the given output file.
     *
     * @param changeLog
     *            change log model
     * @param outputFile
     *            output file
     */
    void writeChangeLog(ChangeLog changeLog, File outputFile);

    /**
     * Marshals a change log model as XML to the given output stream.
     *
     * @param changeLog
     *            change log model
     * @param os
     *            output stream
     */
    void writeChangeLog(ChangeLog changeLog, OutputStream os);
}
