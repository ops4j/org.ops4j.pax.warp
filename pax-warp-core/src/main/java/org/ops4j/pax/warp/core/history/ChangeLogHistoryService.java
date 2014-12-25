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
package org.ops4j.pax.warp.core.history;

import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.ops4j.pax.warp.core.util.Exceptions;
import org.ops4j.pax.warp.jaxb.ChangeLog;
import org.ops4j.pax.warp.jaxb.ChangeSet;
import org.ops4j.pax.warp.jaxb.Column;
import org.ops4j.pax.warp.jaxb.Constraints;
import org.ops4j.pax.warp.jaxb.CreateTable;
import org.ops4j.pax.warp.jaxb.SqlType;
import org.osgi.service.component.annotations.Component;


/**
 * @author Harald Wellmann
 *
 */
@Dependent
@Component(service = ChangeLogHistoryService.class)
public class ChangeLogHistoryService {

    public CreateTable createHistoryTableAction() {
        CreateTable action = new CreateTable();
        action.setTableName("warp_history");
        List<Column> columns = action.getColumn();
        Column id = new Column();
        id.setName("id");
        id.setType(SqlType.VARCHAR);
        id.setLength(20);
        Constraints constraints = new Constraints();
        constraints.setNullable(false);
        id.setConstraints(constraints);
        columns.add(id);

        Column checksum = new Column();
        checksum.setName("checksum");
        checksum.setType(SqlType.CHAR);
        checksum.setLength(64);
        checksum.setConstraints(constraints);
        columns.add(checksum);

        Column executed = new Column();
        executed.setName("executed");
        executed.setType(SqlType.TIMESTAMP);
        columns.add(executed);

        return action;
    }

    public String computeChecksum(ChangeSet changeSet) {
        try {
            JAXBContext context = JAXBContext.newInstance(ChangeLog.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            StringWriter writer = new StringWriter();
            marshaller.marshal(changeSet, writer);


            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            digest.update(writer.toString().getBytes(StandardCharsets.UTF_8));
            byte[] checksum = digest.digest();
            return new BigInteger(1, checksum).toString(16);
        }
        catch (JAXBException | NoSuchAlgorithmException exc) {
            throw Exceptions.unchecked(exc);
        }
    }

    public ChangeLogHistory readChangeLogHistory(Connection dbc) {
        ChangeLogHistory history = new ChangeLogHistory();
        try {
            Statement st = dbc.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, checksum FROM warp_history");
            while (rs.next()) {
                String id = rs.getString("id");
                String checksum = rs.getString("checksum");
                history.put(id, checksum);
            }
            rs.close();
            st.close();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return history;
    }
}
