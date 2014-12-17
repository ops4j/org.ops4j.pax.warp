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
package org.ops4j.pax.warp.command;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.xml.bind.JAXBException;

import org.junit.Test;


/**
 * @author Harald Wellmann
 *
 */
public class CommandRunnerTest {

    private CommandRunner commandRunner = new CommandRunner();

    @Test
    public void shouldUpdateH2() throws JAXBException, SQLException {
        InputStream is = getClass().getResourceAsStream("/changelog1.xml");
        commandRunner.update("jdbc:h2:mem:CommandRunnerTest", null, null, is);
    }

    @Test
    public void shouldUpdateDerby() throws JAXBException, SQLException {
        InputStream is = getClass().getResourceAsStream("/changelog1.xml");
        commandRunner.update("jdbc:derby:memory:CommandRunnerTest;create=true", null, null, is);
    }

    @Test
    public void shouldDumpDataH2() throws JAXBException, SQLException {
        InputStream is = getClass().getResourceAsStream("/changelog1.xml");
        String jdbcUrl = "jdbc:h2:mem:DumpData";
        Connection dbc = DriverManager.getConnection(jdbcUrl);
        commandRunner.update(dbc, is, "h2");

        Statement st = dbc.createStatement();
        st.executeUpdate("insert into NUMBERS (id, i8, i16, i32, i64, d) values (1, 2, 3, 4, 5, 6)");
        st.executeUpdate("insert into NUMBERS (id, i8, i16, i32, i64, d) values (2, 3, 4, 5, 6, 7)");
        st.executeUpdate("insert into NUMBERS (id, i8, i16, i32, i64, d) values (3, 4, 5, 6, 7, 8)");
        st.close();

        commandRunner.dumpData(dbc, System.out);
    }



}
