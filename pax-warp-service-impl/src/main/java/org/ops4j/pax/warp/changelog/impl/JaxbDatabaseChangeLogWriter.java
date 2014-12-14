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
package org.ops4j.pax.warp.changelog.impl;

import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.ops4j.pax.warp.changelog.DatabaseChangeLogWriter;
import org.ops4j.pax.warp.jaxb.DatabaseChangeLog;
import org.ops4j.pax.warp.util.Exceptions;


public class JaxbDatabaseChangeLogWriter implements DatabaseChangeLogWriter {
    private JAXBContext context;

    public JaxbDatabaseChangeLogWriter() throws JAXBException {
        context = JAXBContext.newInstance(DatabaseChangeLog.class);
    }

    @Override
    public void write(DatabaseChangeLog changeLog, Writer writer) {
        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(changeLog, writer);
        }
        catch (JAXBException exc) {
            throw Exceptions.unchecked(exc);
        }
    }
}
