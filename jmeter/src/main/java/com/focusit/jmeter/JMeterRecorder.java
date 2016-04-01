package com.focusit.jmeter;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.RecordingController;
import org.apache.jmeter.protocol.http.proxy.JMeterProxyControl;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestElementTraverser;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Interface to control jmeter proxy recorder
 * @author Denis V. Kirpichenkov
 *
 */
public class JMeterRecorder
{
    private static final String DEFAULT_TEMPLATE_NAME = "template.jmx";

    private static final Logger log = LoggerFactory.getLogger(JMeterRecorder.class);
    private HashTree hashTree;
    private JMeterProxyControl ctrl;
    private RecordingController recCtrl = null;
    private HashTree recCtrlPlace = null;
    private Arguments vars = null;

    private HashTree varsPlace = null;
    private String currentTemplate = DEFAULT_TEMPLATE_NAME;

    public void init() throws Exception
    {
        init(DEFAULT_TEMPLATE_NAME);
    }

    public void init(String pathToTemplate) throws Exception
    {
        this.currentTemplate = pathToTemplate;
        JMeterUtils.setJMeterHome(new File("").getAbsolutePath());
        JMeterUtils.loadJMeterProperties(new File("jmeter.properties").getAbsolutePath());
        JMeterUtils.setProperty("saveservice_properties", File.separator + "saveservice.properties");
        JMeterUtils.setProperty("user_properties", File.separator + "user.properties");
        JMeterUtils.setProperty("upgrade_properties", File.separator + "upgrade.properties");
        JMeterUtils.setProperty("system_properties", File.separator + "system.properties");
        JMeterUtils.setLocale(Locale.ENGLISH);

        JMeterUtils.setProperty("proxy.cert.directory", new File("").getAbsolutePath());
        hashTree = SaveService.loadTree(new File(pathToTemplate));
        ctrl = new JMeterProxyControl();

        hashTree.traverse(new HashTreeTraverser()
        {

            @Override
            public void addNode(Object node, HashTree subTree)
            {
                System.out.println("Node: " + node.toString());

                if (node instanceof Arguments)
                {
                    if (((Arguments)node).getName().equalsIgnoreCase("UDV"))
                    {
                        if (varsPlace == null)
                        {
                            varsPlace = subTree;
                            vars = (Arguments)node;
                        }
                    }
                }

                if (node instanceof RecordingController)
                {
                    if (recCtrl == null)
                    {
                        recCtrl = (RecordingController)node;
                        recCtrlPlace = subTree;
                    }
                }
            }

            @Override
            public void processPath()
            {
            }

            @Override
            public void subtractNode()
            {
            }
        });

        ctrl.setTargetTestElement(recCtrl);
    }

    public void saveScenario(String filename) throws IOException
    {
        TestElement sample = recCtrl.next();
        
        while (sample != null)
        {
            // skip unknown nasty requests
            if(sample instanceof HTTPSamplerBase){
            	HTTPSamplerBase http = (HTTPSamplerBase) sample;
            	if(http.getArguments().getArgumentCount()>0 && http.getArguments().getArgument(0).getValue().startsWith("0Q0O0M0K0I0"))
            	{
            		sample = recCtrl.next();
            		continue;
            	}
            }
            
            final List<TestElement> childs = new ArrayList<>();
            final List<JMeterProperty> keys = new ArrayList<>();
            sample.traverse(new TestElementTraverser()
            {

                @Override
                public void endProperty(JMeterProperty key)
                {
                    if (key.getObjectValue() instanceof HeaderManager)
                    {
                        childs.add((HeaderManager)key.getObjectValue());
                        keys.add(key);
                    }
                }

                @Override
                public void endTestElement(TestElement el)
                {
                }

                @Override
                public void startProperty(JMeterProperty key)
                {
                }

                @Override
                public void startTestElement(TestElement el)
                {
                }
            });

            for (JMeterProperty key : keys)
            {
                sample.removeProperty(key.getName());
            }

            HashTree parent = recCtrlPlace.add(sample);

            for (TestElement child : childs)
            {
                parent.add(child);
            }

            // TODO Groovy script should decide whether add cookie manager or not
            if(sample instanceof HTTPSamplerBase){
            	HTTPSamplerBase http = (HTTPSamplerBase) sample;

                JMeterScriptProcessor.getInstance().processScenario(http, parent, vars);
            }
            sample = recCtrl.next();
        }

        SaveService.saveTree(hashTree, new FileOutputStream(new File(filename)));
    }

    public void startRecording() throws IOException
    {
        JMeterRecorderContext.getInstance().reset();
        ctrl.startProxy();
    }

    public void stopRecording()
    {
        ctrl.stopProxy();
    }

    public void reset() throws IOException {
        hashTree = SaveService.loadTree(new File(currentTemplate));

        hashTree.traverse(new HashTreeTraverser()
        {

            @Override
            public void addNode(Object node, HashTree subTree)
            {
                System.out.println("Node: " + node.toString());

                if (node instanceof Arguments)
                {
                    if (((Arguments)node).getName().equalsIgnoreCase("UDV"))
                    {
                        if (varsPlace == null)
                        {
                            varsPlace = subTree;
                            vars = (Arguments)node;
                        }
                    }
                }

                if (node instanceof RecordingController)
                {
                    if (recCtrl == null)
                    {
                        recCtrl = (RecordingController)node;
                        recCtrlPlace = subTree;
                    }
                }
            }

            @Override
            public void processPath()
            {
            }

            @Override
            public void subtractNode()
            {
            }
        });

        ctrl.setTargetTestElement(recCtrl);
    }
}
