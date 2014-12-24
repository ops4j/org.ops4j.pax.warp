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

import java.io.Reader;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.ops4j.pax.warp.core.changelog.DatabaseChangeLogReader;
import org.ops4j.pax.warp.core.util.Exceptions;
import org.ops4j.pax.warp.jaxb.ChangeLog;

@Dependent
public class JaxbDatabaseChangeLogReader implements DatabaseChangeLogReader {

    @Inject
    private JaxbContext context;

    @Override
    public ChangeLog parse(Reader reader) {
        try {
            Unmarshaller unmarshaller = context.createValidatingUnmarshaller();
            ChangeLog changeLog = (ChangeLog) unmarshaller.unmarshal(reader);
            return changeLog;
        }
        catch (JAXBException exc) {
            throw Exceptions.unchecked(exc);
        }
    }
}
