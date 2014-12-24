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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.ops4j.pax.warp.jaxb.AddPrimaryKey;
import org.ops4j.pax.warp.jaxb.ChangeSet;

/**
 * @author Harald Wellmann
 *
 */
public class ChangeLogHistoryServiceTest {

    private ChangeLogHistoryService historyService = new ChangeLogHistoryService();

    @Test
    public void shouldComputeChecksum() {
        AddPrimaryKey addPk = new AddPrimaryKey();
        addPk.getColumn().add("id");
        ChangeSet changeSet = new ChangeSet();
        changeSet.setId("123");
        changeSet.getChanges().add(addPk);
        assertThat(historyService.computeChecksum(changeSet),
            is("9e872d1c84fe80cc7ae87ac04d7c9e522dee8d3372384203c9e95833e3cf789"));
    }
}
