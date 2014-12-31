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
package org.ops4j.pax.warp.cli;

/**
 * Interface to be implemented by all Pax Warp commands. Each command objects wraps the command line
 * arguments accepted by the given command.
 * <p>
 * The {{@link #run()} method is invoked when the command line arguments have been parsed
 * successfully.
 *
 * @author Harald Wellmann
 *
 */
public interface Command extends Runnable {

    /**
     * Gets the command name. This is the argument which must precede all options and other
     * arguments.
     *
     * @return command name
     */
    String getCommandName();

}
