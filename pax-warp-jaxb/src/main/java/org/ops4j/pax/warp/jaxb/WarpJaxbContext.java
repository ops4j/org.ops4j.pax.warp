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
package org.ops4j.pax.warp.jaxb;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import java.io.IOException;
import java.net.URL;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.ops4j.pax.warp.exc.WarpException;
import org.ops4j.pax.warp.jaxb.gen.ChangeLog;
import org.ops4j.pax.warp.scope.CdiApplicationScoped;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.xml.sax.SAXException;

/**
 * Wraps a JAXB context for the change log model and provides validating marshallers and
 * unmarshallers.
 * <p>
 * Given that a JAXB context is thread-safe and rather expensive to build, this class is a singleton
 * (or application scoped in CDI).
 *
 * @author Harald Wellmann
 *
 */
@Component(service = WarpJaxbContext.class)
@Singleton
@CdiApplicationScoped
public class WarpJaxbContext {

    private JAXBContext context;
    private Schema schema;
    private boolean initialized;

    /**
     * Creates the JAXB context and loads the XML schema.
     * <p>
     * NOTE: When running under Maven/Sisu, {@code PostConstruct} methods are not called
     * automatically.
     */
    @Activate
    @PostConstruct
    protected void init() {
        try {
            context = JAXBContext.newInstance(ChangeLog.class);
            loadSchema();
            initialized = true;
        }
        catch (JAXBException | SAXException | IOException exc) {
            throw new WarpException(exc);
        }
    }

    /**
     * Explicitly initializes this class when running under Maven/Sisu.
     */
    private void initIfNeeded() {
        if (!initialized) {
            init();
        }
    }

    private void loadSchema() throws SAXException, IOException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
        URL url = getClass().getResource("/xsd/warp.xsd");
        schema = schemaFactory.newSchema(new Source[] { new StreamSource(url.openStream()) });
    }

    /**
     * Creates a validating unmarshaller for change logs.
     *
     * @return unmarshaller
     * @throws JAXBException
     *             if the unmarshaller cannot be created
     */
    public Unmarshaller createValidatingUnmarshaller() throws JAXBException {
        initIfNeeded();
        Unmarshaller unmarshaller = context.createUnmarshaller();
        unmarshaller.setSchema(schema);
        return unmarshaller;
    }

    /**
     * Creates a validating pretty-printing marshaller for change logs.
     *
     * @return marshaller
     * @throws JAXBException
     *             if the marshaller cannot be created
     */
    public Marshaller createValidatingMarshaller() throws JAXBException {
        initIfNeeded();
        Marshaller marshaller = context.createMarshaller();
        marshaller.setSchema(schema);
        return marshaller;
    }

    /**
     * Creates a marshaller for change logs fragments. This marshaller does not pretty-print or
     * validate. It can be used for calculating change set checksums.
     *
     * @return marshaller
     * @throws JAXBException
     *             if the marshaller cannot be created
     */
    public Marshaller createFragmentMarshaller() throws JAXBException {
        initIfNeeded();
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        return marshaller;
    }
}
