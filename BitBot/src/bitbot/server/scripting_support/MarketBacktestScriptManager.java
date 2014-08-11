package bitbot.server.scripting_support;

import bitbot.server.scripting.AbstractScriptInteraction;
import javax.script.Invocable;
import javax.script.ScriptEngine;

/**
 *
 * @author z
 */
public class MarketBacktestScriptManager extends AbstractScriptManager {

    public static final void startBacktestScript(String scriptName, boolean CacheScript) {
        String scriptPath = String.format("backtest/%s.js", scriptName);

        try {
            final Invocable iv = getInvocable(scriptPath, CacheScript);
            final ScriptEngine scriptengine = (ScriptEngine) iv;
            if (iv == null) {
                System.out.println("Script not available: " + scriptName);
                return;
            }
            final AbstractScriptInteraction bt = new AbstractScriptInteraction(scriptPath);
            scriptengine.put("bt", bt);

            try {
                iv.invokeFunction("initialize");
                iv.invokeFunction("handle");
            } catch (NoSuchMethodException nsme) {
            }
        } catch (final Exception e) {
            e.printStackTrace();
            System.err.println("Error executing backtest script, name : " + scriptPath + "." + e);
        }
    }
    
    public static final String startBacktestScriptfromString(String script) {
        String ScriptPath = "";
        try {
            final Invocable iv = getInvocableInternal(script, ScriptPath, false);
            final ScriptEngine scriptengine = (ScriptEngine) iv;

            if (iv == null) {
                return "Script not found.";
            }
            final AbstractScriptInteraction bt = new AbstractScriptInteraction(ScriptPath);
            scriptengine.put("bt", bt);

            try {
                iv.invokeFunction("initialize");
                iv.invokeFunction("handle");
            } catch (NoSuchMethodException nsme) {
                return nsme.getMessage();
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        return null;
    }
}
