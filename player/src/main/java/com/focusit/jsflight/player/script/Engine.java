package com.focusit.jsflight.player.script;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONObject;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

public class Engine {
	
	private static final ConcurrentHashMap<Object, Object> context = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, Script> scripts = new ConcurrentHashMap<>();

	private static final GroovyShell shell = new GroovyShell();
	
	private final String script; 
	
	public Engine(String script) {
		super();
		this.script = script;
		if(script!=null && !script.trim().isEmpty()){
			if(scripts.get(script)==null){
				scripts.put(script, shell.parse(script));
			}
		}
	}

	/**
	 * Modifies existing array of events.
	 * Modification defined by script
	 * @param events
	 */
	public void postProcess(List<JSONObject> events){
		Binding binding = new Binding();
		binding.setVariable("context", context);
		binding.setVariable("events", events);
		Script s = scripts.get(script); 
		s.setBinding(binding);
		s.run();
	}
	
	/**
	 * Test of events modification script.
	 * Doesn't modify real loaded events. just use it's clone, modify and println every event to stdout
	 * @param events
	 */
	public void testPostProcess(List<JSONObject> events){
		Binding binding = new Binding();
		binding.setVariable("context", context);
		binding.setVariable("events", new ArrayList<>(events));
		Script s = scripts.get(script); 
		s.setBinding(binding);
		s.run();
	}

	public String getScript() {
		return script;
	}
}
