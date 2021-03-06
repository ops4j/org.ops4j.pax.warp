/*
 * Copyright 2015 Harald Wellmann.
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
package org.ops4j.pax.warp.core.jdbc;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Base64;

import org.junit.Test;

/**
 * Demonstrates usage of {@link Base64}.
 * @author Harald Wellmann
 *
 */
public class Base64Test {

    @Test
    public void base64RoundTrip() {
        String encoded = Base64.getEncoder().encodeToString("Hello world!".getBytes(UTF_8));
        assertThat(encoded, is("SGVsbG8gd29ybGQh"));
        String decoded = new String(Base64.getDecoder().decode(encoded), UTF_8);
        assertThat(decoded, is("Hello world!"));
    }
}
