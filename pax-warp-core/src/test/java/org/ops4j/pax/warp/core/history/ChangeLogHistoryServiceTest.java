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

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.warp.core.dbms.DerbyProfile;
import org.ops4j.pax.warp.core.update.impl.UpdateSqlGenerator;
import org.ops4j.pax.warp.jaxb.WarpJaxbContext;
import org.ops4j.pax.warp.jaxb.gen.AddPrimaryKey;
import org.ops4j.pax.warp.jaxb.gen.ChangeSet;

/**
 * @author Harald Wellmann
 *
 */
@RunWith(PaxExam.class)
public class ChangeLogHistoryServiceTest {

    @Inject
    private WarpJaxbContext context;

    @Test
    public void shouldComputeChecksum() {
        UpdateSqlGenerator generator = new UpdateSqlGenerator(new DerbyProfile(), null,
            c -> {}, context);
        AddPrimaryKey addPk = new AddPrimaryKey();
        addPk.getColumn().add("id");
        ChangeSet changeSet = new ChangeSet();
        changeSet.setId("123");
        changeSet.getChanges().add(addPk);
        assertThat(generator.computeChecksum(changeSet),
            is("9604e481f9710d27926e6022aa4fc2a4ebd676d97f59bf9f7c7ae42f74911ece"));
    }
}
