package com.focusit.jmeter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

public class MainApp {
	public static void main(String args[]) throws IOException, IllegalUserActionException {
		JMeterUtils.setJMeterHome(new File("").getAbsolutePath());
		JMeterUtils.loadJMeterProperties("jmeter.properties");
		JMeterUtils.setProperty("saveservice_properties", File.separator+"saveservice.properties");
		SaveService save = new SaveService();		

		// Engine
		StandardJMeterEngine jm = new StandardJMeterEngine();
		// jmeter.properties
		// JMeterUtils.loadJMeterProperties("c:/tmp/jmeter.properties");
		HashTree hashTree = new HashTree();
		hashTree = save.loadTree(new File("template.jmx"));
		
		jm.configure(hashTree);
		
//		GuiPackage.getInstance(new JMeterTreeListener(), new JMeterTreeModel()).addSubTree(hashTree);
		ProxyControl ctrl = new ProxyControl();
//		ctrl.startProxy();
		save.saveTree(hashTree, new FileOutputStream(new File("test.jmx")));
		System.out.println("Starting");
	}
}
