/*
 * Copyright 2016 OPS4J Contributors
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;

import javax.xml.bind.Unmarshaller;

import org.junit.Test;
import org.ops4j.pax.warp.jaxb.WarpJaxbContext;
import org.ops4j.pax.warp.jaxb.gen.ChangeLog;
import org.ops4j.pax.warp.jaxb.gen.ChangeSet;
import org.ops4j.pax.warp.jaxb.gen.ColumnValue;
import org.ops4j.pax.warp.jaxb.gen.Insert;

/**
 * Tests whitespace preservation on marshalling and unmarshalling change logs.
 * 
 * @author Harald Wellmann
 *
 */
public class JaxbChangeLogWriterTest {

    @Test
    public void writeChangeLogWithWhitespace() throws Exception {
        ChangeLog changeLog = new ChangeLog();
        changeLog.setVersion("0.1");
        ChangeSet changeSet = new ChangeSet();
        changeSet.setId("1");
        Insert insert = new Insert();
        insert.setTableName("foo");
        ColumnValue column = new ColumnValue();
        column.setName("name");
        column.setType("VARCHAR");
        column.setValue("  One\r\nTwo\r\n  ");
        insert.getColumn().add(column);
        changeSet.getChanges().add(insert);
        changeLog.getChangeSet().add(changeSet);

        JaxbChangeLogWriter writer = new JaxbChangeLogWriter();
        WarpJaxbContext context = new WarpJaxbContext();
        writer.setContext(context);
        File outputFile = new File("target/out.xml");
        try (OutputStream os = new FileOutputStream(outputFile)) {
            writer.writeChangeLog(changeLog, os);
        }

        Unmarshaller unmarshaller = context.createValidatingUnmarshaller();
        ChangeLog cl = (ChangeLog) unmarshaller.unmarshal(outputFile);
        Insert ins = (Insert) cl.getChangeSet().get(0).getChanges().get(0);
        String value = ins.getColumn().get(0).getValue();
        assertThat(value, is("  One\r\nTwo\r\n  "));

        JaxbChangeLogReader reader = new JaxbChangeLogReader();
        reader.setContext(context);
        cl = reader.parse(new FileReader(outputFile));
        ins = (Insert) cl.getChangeSet().get(0).getChanges().get(0);
        value = ins.getColumn().get(0).getValue();
        assertThat(value, is("  One\r\nTwo\r\n  "));
    }
}
