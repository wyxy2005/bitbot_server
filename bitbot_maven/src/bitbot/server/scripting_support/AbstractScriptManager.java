package bitbot.server.scripting_support;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

/**
 *
 * @author z
 */
public abstract class AbstractScriptManager {

    private static final ReadWriteLock mutex = new ReentrantReadWriteLock();
    private static final Map<String, String> ScriptCache = new HashMap();
    private final static ScriptEngineFactory sef = new ScriptEngineManager().getEngineByName("javascript").getFactory();

    protected static Invocable getInvocable(String path, boolean CacheScript) {
        path = String.format("scripts/%s", path);
        if (CacheScript) {
            String script = ScriptCache.get(path);
            if (script != null) {
                try {
                    return getInvocableInternal(script, path, CacheScript);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println(String.format("Error executing script. [%s]", path));
                }
                return null;
            }
        }
        File scriptFile = new File(path);

        if (!scriptFile.exists()) {
            return null;
        }
        char[] chars = new char[(int) scriptFile.length()];
        try (final FileReader fr = new FileReader(scriptFile)) {
            fr.read(chars, 0, chars.length);

            final String script = new String(chars);

            return getInvocableInternal(script, path, CacheScript);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(String.format("Error executing script. [%s]", path));
        }
        return null;
    }

    protected static Invocable getInvocableInternal(String script, String path, boolean CacheScript) throws Exception {
        final ScriptEngine engine2 = sef.getScriptEngine();
        final CompiledScript compiled = ((Compilable) engine2).compile(script);
        compiled.eval();
        
        if (CacheScript) {
            mutex.writeLock().lock();
            try {
                ScriptCache.put(path, script);
            } finally {
                mutex.writeLock().unlock();
            }
        }
        return (Invocable) engine2;
    }

    public static final void reloadCachedScript() {
        mutex.writeLock().lock();
        try {
            ScriptCache.clear();
        } finally {
            mutex.writeLock().unlock();
        }
    }
}
