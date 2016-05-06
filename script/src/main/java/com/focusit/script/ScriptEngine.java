package com.focusit.script;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Groovy script "compiler"
 * Class parses the script into some intermediate form and hold a reference to it.
 * Also class provides a thread local storage for scripts.
 * Created by doki on 06.04.16.
 */
public class ScriptEngine {
    // General purpose script storage.
    // Not Thread Safe, because compiled script has writable bindings. So one thread can easily change other one's bindings
    private final ConcurrentHashMap<String, Script> generalScripts = new ConcurrentHashMap<>();
    private final ClassLoader loader = new ScriptsClassLoader(ClassLoader.getSystemClassLoader());
    private final GroovyShell shell = new GroovyShell(loader);
    private static final ScriptEngine instance = new ScriptEngine();
    private final Script NO_SCRIPT = new Script() {
        @Override
        public Object run() {
            return null;
        }
    };
    // Storage to hold compiled script(s) in thread's bounds. Thread safe!
    // One script for one thread. No way to manipulate script's bindings outside calling thread
    private ThreadLocal<HashMap<String, Script>> threadBindedScripts = new ThreadLocal();

    private ScriptEngine() {
    }

    public static ScriptEngine getInstance(){
        return instance;
    }

    public ClassLoader getClassLoader(){
        return this.loader;
    }

    @Nullable
    public Script getScript(String script){
        if(script==null) {
            return null;
        }

        if(script.isEmpty()) {
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
            result.setBinding(new Binding());
            scripts.put(script, result);
        }

        return result;
    }
}
