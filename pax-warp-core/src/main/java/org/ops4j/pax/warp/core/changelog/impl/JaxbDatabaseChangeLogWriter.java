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
package org.ops4j.pax.warp.core.changelog.impl;

import java.io.Writer;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.ops4j.pax.warp.core.changelog.DatabaseChangeLogWriter;
import org.ops4j.pax.warp.core.util.Exceptions;
import org.ops4j.pax.warp.jaxb.ChangeLog;


@Dependent
public class JaxbDatabaseChangeLogWriter implements DatabaseChangeLogWriter {

    @Inject
    private JaxbContext context;

    @Override
    public void write(ChangeLog changeLog, Writer writer) {
        try {
            Marshaller marshaller = context.createValidatingMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(changeLog, writer);
        }
        catch (JAXBException exc) {
            throw Exceptions.unchecked(exc);
        }
    }
}
