package com.focusit.jmeter;

import java.io.IOException;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.protocol.http.control.RecordingController;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jorphan.collections.HashTree;

public class MainApp {
	public static void main(String args[]) throws IOException, IllegalUserActionException {
		// Engine
		StandardJMeterEngine jm = new StandardJMeterEngine();
		// jmeter.properties
		// JMeterUtils.loadJMeterProperties("c:/tmp/jmeter.properties");
		HashTree hashTree = new HashTree();
		
		// HTTP Sampler
//        HTTPSampler2 httpSampler = new HTTPSampler2();
        TestElement recCtrl = new RecordingController();

        // Loop Controller
        LoopController loopCtrl = new LoopController();
        ((LoopController)loopCtrl).setLoops(1);
        ((LoopController)loopCtrl).addTestElement(recCtrl);
        ((LoopController)loopCtrl).setFirst(true);
        
		// Thread Group
		SetupThreadGroup threadGroup = new SetupThreadGroup();
		threadGroup.setNumThreads(1);
		threadGroup.setRampUp(1);
		threadGroup.setSamplerController(loopCtrl);

		// Test plan
		TestPlan testPlan = new TestPlan("MY TEST PLAN");
		hashTree.add("testPlan", testPlan);
		hashTree.add("threadGroup", threadGroup);
		jm.configure(hashTree);
		
//		GuiPackage.getInstance(new JMeterTreeListener(), new JMeterTreeModel()).addSubTree(hashTree);
		ProxyControl ctrl = new ProxyControl();
		ctrl.startProxy();
		System.out.println("Starting");
	}
}
