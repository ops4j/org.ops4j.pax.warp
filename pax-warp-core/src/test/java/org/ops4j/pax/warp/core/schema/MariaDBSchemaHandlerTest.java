package org.ops4j.pax.warp.core.schema;

import org.junit.Before;
import org.ops4j.pax.warp.core.dbms.MysqlDbmsAdapter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assume.assumeThat;

/**
 * Created by tonit on 28/11/2016.
 */
public class MariaDBSchemaHandlerTest extends AbstractSchemaHandlerTest {


    @Before
    public void before() {
        assumeThat(System.getProperty("mariadb"), is("true"));
    }

    public MariaDBSchemaHandlerTest() {
        super(new MysqlDbmsAdapter());
    }
}
