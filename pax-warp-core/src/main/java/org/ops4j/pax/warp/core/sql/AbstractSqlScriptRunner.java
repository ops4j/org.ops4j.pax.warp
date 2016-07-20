/*
 * Copyright 2013 Harald Wellmann.
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
package org.ops4j.pax.warp.core.sql;

import static org.ops4j.pax.warp.core.sql.AbstractSqlScriptRunner.LexerStatus.COMPLETE;
import static org.ops4j.pax.warp.core.sql.AbstractSqlScriptRunner.LexerStatus.EOF;
import static org.ops4j.pax.warp.core.sql.AbstractSqlScriptRunner.LexerStatus.IN_COMMENT;
import static org.ops4j.pax.warp.core.sql.AbstractSqlScriptRunner.LexerStatus.IN_STRING;
import static org.ops4j.pax.warp.core.sql.AbstractSqlScriptRunner.LexerStatus.MINUS_SEEN;
import static org.ops4j.pax.warp.core.sql.AbstractSqlScriptRunner.LexerStatus.NORMAL;
import static org.ops4j.pax.warp.core.sql.AbstractSqlScriptRunner.LexerStatus.QUOTE_SEEN;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.ops4j.pax.warp.core.exc.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes an SQL script in a context defined by a concrete implementation class.
 * <p>
 * The script is split into statements which are executed one after another. The statements may not
 * return any results, i.e. SELECT statements are not supported.
 * <p>
 * The script parser is rather dumb and does not recognize any SQL syntax, except comments and
 * literal strings. It simply treats each semicolon outside of a literal string as a statement
 * terminator.
 * <p>
 * For this to work, make sure to terminate each statement by a semicolon.
 * <p>
 * The ScriptRunner supports dialect and version specific variants of a script, similar to locale
 * specific lookup of resource bundles.
 * <p>
 * If a dialect and a version are set on the ScriptRunner, given a script name /path/to/foo.sql, the
 * runner will execute the first match in the following list:
 * <ul>
 * <li>/path/to/version/dialect/foo.sql</li>
 * <li>/path/to/version/foo.sql</li>
 * <li>/path/to/dialect/foo.sql</li>
 * <li>/path/to/foo.sql</li>
 * </ul>
 * This class is not thread-safe.
 * 
 * @author Harald Wellmann
 * 
 */
public abstract class AbstractSqlScriptRunner {

    private static Logger log = LoggerFactory.getLogger(AbstractSqlScriptRunner.class);

    private boolean terminateOnError = true;

    private String version = "";

    private String dialect = "";

    private LexerStatus status = NORMAL;

    enum LexerStatus {
        NORMAL,
        MINUS_SEEN,
        QUOTE_SEEN,
        IN_COMMENT,
        IN_STRING,
        COMPLETE,
        EOF
    }

    /**
     * Returns the current value of the termination flag.
     * 
     * @return termination flag.
     */
    public boolean getTerminateOnError() {
        return terminateOnError;
    }

    /**
     * Sets the termination flag. If true, any SQLException from an executed statement will be
     * propagated. Otherwise, the exception will be logged and the runner will continue with the
     * next statement.
     * 
     * @param enabled
     *            stop on error
     */
    public void setTerminateOnError(boolean enabled) {
        this.terminateOnError = enabled;
    }

    /**
     * Executes a script from the given reader.
     * 
     * @param reader
     *            script reader
     * @throws IOException
     *             on read error
     */
    public void executeScript(Reader reader) throws IOException {
        StringBuilder command = new StringBuilder();
        while (status != EOF) {
            int c = reader.read();
            if (status == MINUS_SEEN) {
                status = consumeWhenMinusSeen(command, c);
            }
            else if (status == IN_COMMENT) {
                status = consumeInComment(c);
            }
            else if (status == IN_STRING) {
                status = consumeInString(command, c);
            }
            else if (status == QUOTE_SEEN) {
                status = consumeWhenQuoteSeen(command, c);
            }
            else {
                status = consumeDefault( command, c);
                if (status == COMPLETE) {
                    runStatement(command.toString());
                    command = new StringBuilder();
                    status = NORMAL;
                }
            }
        }
        reader.close();
    }

    private LexerStatus consumeDefault(StringBuilder command, int c) {
        switch (c) {
            case -1:
                status = EOF;
                break;

            case '-':
                status = MINUS_SEEN;
                break;

            case '\'':
                status = IN_STRING;
                command.append((char) c);
                break;

            case ';':
                status = COMPLETE;
                break;

            default:
                command.append((char) c);
        }
        return status;
    }

    private LexerStatus consumeWhenQuoteSeen(StringBuilder command, int c) {
        switch (c) {
            case -1:
                status = EOF;
                break;

            case '\'':
                status = IN_STRING;
                command.append((char) c);
                break;

            default:
                status = NORMAL;
                command.append((char) c);
        }
        return status;
    }

    private LexerStatus consumeInString(StringBuilder command, int c) {
        switch (c) {
            case -1:
                status = EOF;
                break;

            case '\'':
                status = QUOTE_SEEN;
                command.append((char) c);
                break;

            default:
                command.append((char) c);
        }
        return status;
    }

    private LexerStatus consumeInComment(int c) {
        switch (c) {
            case -1:
                status = EOF;
                break;

            case '\n':
                status = NORMAL;
                break;
            default:
                // nothing
        }
        return status;
    }

    private LexerStatus consumeWhenMinusSeen(StringBuilder command, int c) {
        switch (c) {
            case -1:
                status = EOF;
                break;

            case '-':
                status = IN_COMMENT;
                break;

            default:
                status = NORMAL;
                command.append('-');
                command.append((char) c);
        }
        return status;
    }
    

    /**
     * Executes a script obtained as a resource relative to given class with a given resource name,
     * possibly interpolated with dialect and version information.
     * 
     * @param clazz
     *            class for resource lookup
     * @param resourceName
     *            resource path
     */
    public void executeScript(Class<?> clazz, String resourceName) {
        URL url = getVersionedUrl(clazz, resourceName);

        if (url == null) {
            throw new IllegalArgumentException("file " + resourceName + " not found.");
        }
        log.debug("resource name = {}, url = {}", resourceName, url);
        try {
            InputStream is = url.openStream();
            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            executeScript(new BufferedReader(reader));
        }
        catch (IOException exc) {
            throw Exceptions.unchecked(exc);
        }
    }

    /**
     * Interpolates a resource name with dialect and version information and returns the first
     * matching resource URL.
     * 
     * @param clazz
     *            class for resource lookup
     * @param resourceName
     *            resource name
     * @return
     */
    private URL getVersionedUrl(Class<?> clazz, String resourceName) {
        URL url = constructUrl(clazz, resourceName, true, true);
        if (url != null) {
            return url;
        }

        url = constructUrl(clazz, resourceName, true, false);
        if (url != null) {
            return url;
        }

        url = constructUrl(clazz, resourceName, false, true);
        if (url != null) {
            return url;
        }

        return constructUrl(clazz, resourceName, false, false);
    }

    private URL constructUrl(Class<?> clazz, String resourceName, boolean hasVersion,
        boolean hasDialect) {
        int slash = resourceName.lastIndexOf("/");
        String root = (slash == -1) ? "" : resourceName.substring(0, slash + 1);
        StringBuilder sb = new StringBuilder(root);
        if (hasVersion) {
            sb.append(version).append("/");
        }
        if (hasDialect) {
            sb.append(dialect).append("/");
        }
        sb.append(resourceName.substring(slash + 1));
        log.debug("construced path {}", sb.toString());
        URL url = clazz.getResource(sb.toString());
        return url;
    }

    /**
     * Runs a single statement from the script.
     * 
     * @param sql
     *            SQL statement
     */
    protected abstract void runStatement(String sql);

    /**
     * Sets a version string for script lookup.
     * 
     * @param version
     *            script version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Sets an SQL dialect string for script lookup.
     * 
     * @param dialect
     *            SQL dialect
     */
    public void setDialect(String dialect) {
        this.dialect = dialect;
    }
}
