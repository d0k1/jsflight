package com.focusit.jsflight.player.scenario;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.focusit.jsflight.player.input.Events;

public class UserScenario {
    private static final String SET_ELEMENT_VISIBLE_JS = "var e = document.evaluate('%s' ,document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null ).singleNodeValue; if(e!== null) {e.style.visibility='visible';};";
    private List<JSONObject> events = new ArrayList<>();
    private List<Boolean> checks;

	public void setRawevents(Events rawevents) {
	}
	
	public void rewind(){
		
	}
	
	public void prev(){
		
	}
	
	public void next(){
		
	}
	
	public void skip(){
		
	}
	
	public void deleteStep(){
		
	}
	
	public void copyStep(){
		
	}
	
	public void play(){
		
	}
	
	public void postProcessScenario(){
		
	}
	
	public void preprocessStep(){
		
	}
	
	public void postProcessStep(){
		
	}
}
