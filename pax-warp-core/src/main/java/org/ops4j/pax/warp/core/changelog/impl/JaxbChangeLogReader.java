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

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.ops4j.pax.warp.core.changelog.ChangeLogReader;
import org.ops4j.pax.warp.exc.WarpException;
import org.ops4j.pax.warp.jaxb.WarpJaxbContext;
import org.ops4j.pax.warp.jaxb.gen.ChangeLog;
import org.ops4j.pax.warp.scope.CdiDependent;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
@CdiDependent
@Named
public class JaxbChangeLogReader implements ChangeLogReader {

    @Inject
    private WarpJaxbContext context;

    @Override
    public ChangeLog parse(Reader reader) {
        try {
            Unmarshaller unmarshaller = context.createValidatingUnmarshaller();
            return (ChangeLog) unmarshaller.unmarshal(reader);
        }
        catch (JAXBException exc) {
            throw new WarpException(exc);
        }
    }

    @Reference
    public void setContext(WarpJaxbContext context) {
        this.context = context;
    }
}
