package com.focusit.jmeter;

import java.io.File;
import java.io.IOException;

import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

public class JMeterRecorder {
	private HashTree hashTree;
	ProxyControl ctrl;
	
	public void init() throws IOException{
		JMeterUtils.setJMeterHome(new File("").getAbsolutePath());
		JMeterUtils.loadJMeterProperties("jmeter.properties");
		JMeterUtils.setProperty("saveservice_properties", File.separator+"saveservice.properties");
		hashTree = SaveService.loadTree(new File("template.jmx"));
		ctrl = new ProxyControl();
		hashTree.list();
	}
	
	public void startRecording(){
		
	}
	
	public void stopRecording(){
		
	}
	
	public void saveScenario(String filename){
		
	}
}
