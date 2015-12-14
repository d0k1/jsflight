package com.focusit.jsflight.player.script;

import com.focusit.jsflight.player.input.Events;

public class Engine {

	private final String script; 
	
	public Engine(String script) {
		super();
		this.script = script;
	}

	/**
	 * Modifies existing array of events.
	 * Modification defined by script
	 * @param events
	 */
	public void postProcess(Events events){
		
	}
	
	/**
	 * Test of events modification script.
	 * Doesn't modify real loaded events. just use it's clone, modify and println every event to stdout
	 * @param events
	 */
	public void testPostProcess(Events events){
		
	}

	public String getScript() {
		return script;
	}
}
