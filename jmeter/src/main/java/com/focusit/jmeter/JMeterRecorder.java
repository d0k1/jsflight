package com.focusit.jmeter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.RecordingController;
import org.apache.jmeter.protocol.http.proxy.JMeterProxyControl;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestElementTraverser;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;

/**
 * Interface to control jmeter proxy recorder
 * @author Denis V. Kirpichenkov
 *
 */
public class JMeterRecorder {
	private HashTree hashTree;
	JMeterProxyControl ctrl;
	RecordingController target = null;
	HashTree recPlace = null;

	public void init() throws Exception {
		JMeterUtils.setJMeterHome(new File("").getAbsolutePath());
		JMeterUtils.loadJMeterProperties(new File("jmeter.properties").getAbsolutePath());
		JMeterUtils.setProperty("saveservice_properties", File.separator + "saveservice.properties");
		JMeterUtils.setProperty("user_properties", File.separator + "user.properties");
		JMeterUtils.setProperty("upgrade_properties", File.separator + "upgrade.properties");
		JMeterUtils.setLocale(Locale.ENGLISH);

		JMeterUtils.setProperty("proxy.cert.directory", new File("").getAbsolutePath());
		hashTree = SaveService.loadTree(new File("template.jmx"));
		ctrl = new JMeterProxyControl();

		hashTree.traverse(new HashTreeTraverser() {

			@Override
			public void subtractNode() {
			}

			@Override
			public void processPath() {
			}

			@Override
			public void addNode(Object node, HashTree subTree) {
				if (node instanceof RecordingController) {
					if (target == null) {
						target = (RecordingController) node;
						recPlace = subTree;
					}
				}
			}
		});

		ctrl.setTargetTestElement(target);
	}

	public void saveScenario(String filename) throws IOException {
		TestElement sample = target.next();

		while(sample!=null){
			final TestElement sample1 = sample;
			final List<TestElement> childs = new ArrayList<>();
			final List<JMeterProperty> keys = new ArrayList<>();
			sample.traverse(new TestElementTraverser() {
				
				@Override
				public void startTestElement(TestElement el) {
				}
				
				@Override
				public void startProperty(JMeterProperty key) {
				}
				
				@Override
				public void endTestElement(TestElement el) {
				}
				
				@Override
				public void endProperty(JMeterProperty key) {
					if(key.getObjectValue() instanceof HeaderManager){
						childs.add((TestElement)key.getObjectValue());
						keys.add(key);
					}
				}
			});
			
			for(JMeterProperty key:keys){
				sample.removeProperty(key.getName());
			}
			
			HashTree parent = recPlace.add(sample, sample); 
			for(TestElement child:childs){
				parent.add(child, child);
			}
			
			sample = target.next();
		}
		
		SaveService.saveTree(hashTree, new FileOutputStream(new File(filename)));
	}

	public void startRecording() throws IOException {
		ctrl.startProxy();
	}

	public void stopRecording() {
		ctrl.stopProxy();
	}
}
