package org.ops4j.pax.warp.core.trimou;

import java.util.HashMap;
import java.util.Map;

/**
 * To avoid reinitializing the Mustache engine multiple times, this class caches the template
 * engines.
 *
 * @author Kevin Grüneberg
 */
public class TemplateEngineSelector {

    private static Map<String, TemplateEngine> templateEngines = new HashMap<>();

    private TemplateEngineSelector() {
        // Hidden utility class constructor
    }

    /**
     * Gets a template engine for the given JDBC subprotocol.
     * @param subprotocol JDBC subprotocol
     * @return template engine
     */
    public synchronized static TemplateEngine getTemplateEngine(String subprotocol) {
        if (!templateEngines.containsKey(subprotocol)) {
            initTemplateEngine(subprotocol);
        }

        return templateEngines.get(subprotocol);
    }

    private static void initTemplateEngine(String subprotocol) {
        TemplateEngine templateEngine = new TemplateEngine(subprotocol);
        templateEngines.put(subprotocol, templateEngine);
    }
}
