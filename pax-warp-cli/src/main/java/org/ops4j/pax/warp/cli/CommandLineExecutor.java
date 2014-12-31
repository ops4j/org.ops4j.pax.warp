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

import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

/**
 * Parses the command line and invokes the appropriate command. Each command must implement the
 * {@link Command} interfaces and contain JCommander annotations for the accepted options and
 * arguments.
 *
 * @author Harald Wellmann
 *
 */
@Dependent
public class CommandLineExecutor {

    private static Logger log = LoggerFactory.getLogger(CommandLineExecutor.class);

    private JCommander commander;

    @Inject
    @Any
    private Instance<Command> commands;

    @PostConstruct
    private void init() {
        SortedMap<String, Command> commandMap = new TreeMap<>();
        commands.forEach(c -> commandMap.put(c.getCommandName(), c));
        commander = new JCommander();
        commandMap.forEach((k, v) -> commander.addCommand(k, v));
    }

    /**
     * Parses the command line, selects the appropriate command and executes it.
     *
     * @param args
     *            command line arguments
     */
    public void execute(String[] args) {
        try {
            commander.parse(args);
            String commandName = commander.getParsedCommand();
            JCommander subCommander = commander.getCommands().get(commandName);
            Runnable command = (Runnable) subCommander.getObjects().get(0);
            command.run();
        }
        catch (ParameterException exc) {
            log.error(exc.getMessage());
            commander.usage();
        }
        // CHECKSTYLE:SKIP
        catch (Exception exc) {
            log.error("exception running command", exc);
        }
    }
}
