/*
 * Copyright 2016 Harald Wellmann.
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
package org.ops4j.pax.warp.core.dbms;




/**
 * @author Harald Wellmann
 *
 */
public class OracleProfile implements DbmsProfile {

    @Override
    public String getSubprotocol() {
        return "oracle";
    }

    @Override
    public boolean getTableNameIsCaseSensitive() {
        return true;
    }

    @Override
    public boolean isGeneratedIndex(String indexName) {
        return indexName.equalsIgnoreCase("PRIMARY") || indexName.toUpperCase().startsWith("FK_");
    }

    @Override
    public boolean getSchemaIsCatalog() {
        return true;
    }
    
    @Override
    public boolean getAutoIncrementNeedsTrigger() {
        return true;
    }

    @Override
    public boolean getAutoIncrementNeedsSequence() {
        return true;
    }

    @Override
    public boolean getAutoIncrementHasMetadata() {
        return false;
    }

    @Override
    public boolean getEmptyStringIsNull() {
        return true;
    }
}
