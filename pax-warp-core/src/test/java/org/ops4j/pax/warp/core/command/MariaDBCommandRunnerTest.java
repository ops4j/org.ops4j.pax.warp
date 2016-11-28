package org.ops4j.pax.warp.core.command;

import org.junit.Before;
import org.ops4j.pax.warp.core.dbms.MariaDBDbmsAdapter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assume.assumeThat;

/**
 * Created by tonit on 28/11/2016.
 */
public class MariaDBCommandRunnerTest extends AbstractCommandRunnerTest {

    @Before
    public void before() {
        assumeThat(System.getProperty("mariadb"), is("true"));
    }

    public MariaDBCommandRunnerTest() {
        super(new MariaDBDbmsAdapter());
    }
}
