package com.focusit.jmeter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
		ctrl = new ProxyControl();	
	}
	
	public void startRecording() throws IOException{
		ctrl.startProxy();
	}
	
	public void stopRecording(){
		ctrl.stopProxy();
	}
	
	public void saveScenario(String filename) throws IOException{
		SaveService.saveTree(hashTree, new FileOutputStream(new File(filename)));
	}
}
