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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.ops4j.pax.warp.core.changelog.ChangeLogWriter;
import org.ops4j.pax.warp.exc.WarpException;
import org.ops4j.pax.warp.jaxb.WarpJaxbContext;
import org.ops4j.pax.warp.jaxb.gen.ChangeLog;
import org.ops4j.pax.warp.scope.CdiDependent;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;


/**
 * Implements {@link ChangeLogWriter} using a JAXB marshaller.
 *
 * @author Harald Wellmann
 *
 */
@Component
@CdiDependent
@Named
public class JaxbChangeLogWriter implements ChangeLogWriter {

    @Inject
    private WarpJaxbContext context;

    @Override
    public void write(ChangeLog changeLog, Writer writer) {
        try {
            Marshaller marshaller = context.createValidatingMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(changeLog, writer);
        }
        catch (JAXBException exc) {
            throw new WarpException(exc);
        }
    }

    @Override
    public void writeChangeLog(ChangeLog changeLog, File outputFile) {
        try (OutputStream os = new FileOutputStream(outputFile)) {
            writeChangeLog(changeLog, os);
        }
        catch (IOException exc) {
            throw new WarpException(exc);
        }
    }

    @Override
    public void writeChangeLog(ChangeLog changeLog, OutputStream os) {
        try {
            Marshaller marshaller = context.createValidatingMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(changeLog, os);
        }
        catch (JAXBException exc) {
            throw new WarpException(exc);
        }
    }




    /**
     * Sets the JAXB context.
     *
     * @param context
     *            JAXB context for change log model
     */
    @Reference
    public void setContext(WarpJaxbContext context) {
        this.context = context;
    }


}
