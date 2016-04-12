/*
 * Copyright 2016 EOS UPTRADE GmbH.
 *
 * This is proprietary software. All rights reserved. Unauthorized use is prohibited.
 */
package org.ops4j.pax.warp.core.dbms;

import java.sql.SQLException;

/**
 * @author hwellmann
 *
 */
public interface DbmsAdapter {

    String getJdbcUrl();

    String getJdbcAdminUrl();

    String getSubprotocol();

    default String getDefaultSchema(){
        return "warp";
    }

    void dropAndCreateDatabase() throws SQLException;

}
