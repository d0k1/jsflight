package com.focusit.script;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Groovy script "compiler"
 * Class parses the script into some intermediate form and hold a reference to it.
 * Also class provides a thread local storage for scripts.
 * Created by doki on 06.04.16.
 */
public class ScriptEngine {
    // general purpose script storage. Not Thread Safe!
    private final static ConcurrentHashMap<String, Script> generalScripts = new ConcurrentHashMap<>();
    private static final ClassLoader loader = new ScriptsClassLoader(ClassLoader.getSystemClassLoader());
    private static final GroovyShell shell = new GroovyShell(loader);
    private static final ScriptEngine instance = new ScriptEngine();
    private static final Script NO_SCRIPT = new Script() {
        @Override
        public Object run() {
            return null;
        }
    };
    // Storage to hold compiled script(s) in thread's bounds. Thread safe!
    private ThreadLocal<HashMap<String, Script>> threadBindedScripts = new ThreadLocal();

    private ScriptEngine() {
    }

    public static ScriptEngine getInstance(){
        return instance;
    }

    public ClassLoader getClassLoader(){
        return ScriptEngine.loader;
    }

    public Script getScript(String script){
        if(script==null) {
            return null;
        }

        Script result = generalScripts.get(script);

        if(result==null){
            if(script.trim().length()>0) {
                result = shell.parse(script);
            } else {
                result = NO_SCRIPT;
            }
            generalScripts.put(script, result);
        }

        if(result.equals(NO_SCRIPT)) {
            return null;
        }

        return result;
    }

    public Script getThreadBindedScript(String script){

        if(script==null) {
            return null;
        }

        HashMap<String, Script> scripts = threadBindedScripts.get();
        if(scripts==null) {
            scripts = new HashMap<>();
            threadBindedScripts.set(scripts);
        }

        Script result = scripts.get(script);

        if(result==null) {
            result = shell.parse(script);
            scripts.put(script, result);
        }

        return result;
    }
}
