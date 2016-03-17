package com.focusit.jmeter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.Cookie;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.Header;
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

/**
 * Interface to control jmeter proxy recorder
 * @author Denis V. Kirpichenkov
 *
 */
public class JMeterRecorder
{
    private HashTree hashTree;
    JMeterProxyControl ctrl;
    RecordingController recCtrl = null;
    HashTree recCtrlPlace = null;
    Arguments vars = null;
    HashTree varsPlace = null;

    public void init() throws Exception
    {
        JMeterUtils.setJMeterHome(new File("").getAbsolutePath());
        JMeterUtils.loadJMeterProperties(new File("jmeter.properties").getAbsolutePath());
        JMeterUtils.setProperty("saveservice_properties", File.separator + "saveservice.properties");
        JMeterUtils.setProperty("user_properties", File.separator + "user.properties");
        JMeterUtils.setProperty("upgrade_properties", File.separator + "upgrade.properties");
        JMeterUtils.setLocale(Locale.ENGLISH);

        JMeterUtils.setProperty("proxy.cert.directory", new File("").getAbsolutePath());
        hashTree = SaveService.loadTree(new File("template.jmx"));
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

            CookieManager cookies = new CookieManager();
            cookies.setName("HTTP Cookie Manager");
            cookies.setEnabled(true);
            cookies.setProperty(TestElement.GUI_CLASS, "CookiePanel");
            cookies.setProperty(TestElement.TEST_CLASS, "CookieManager");
            parent.add(cookies);

            for (TestElement child : childs)
            {
                for (int i = 0; i < ((HeaderManager)child).getHeaders().size(); i++)
                {
                    JMeterProperty prop = ((HeaderManager)child).getHeaders().get(i);
                    if (prop.getName().equalsIgnoreCase("cookie"))
                    {
                        Header val = (Header)prop.getObjectValue();
                        
                        // TODO groovy script should parse cookies and do something great                        
                        String cooks = val.getValue().toString();
                        
                        String pattern = "employee=(\\w+)\\$(\\w+)";
                        Pattern r = Pattern.compile(pattern);
                        Matcher m = r.matcher(cooks);
                        if(m.find()){
                        	String name = "jsid_"+m.group(1)+"_"+m.group(2);
                            cookies.add(new Cookie("JSESSIONID", "${"+name+"}", sample.getPropertyAsString(HTTPSamplerBase.DOMAIN), "/",
                                    false, 0L));
                            
                            if(!vars.getArgumentsAsMap().containsKey(name)) {
                            	vars.addArgument(new Argument(name, "empty_session"));
                            }
                        }
                        ((HeaderManager)child).remove(i);
                    }
                }
                parent.add(child);
            }

            sample = recCtrl.next();
        }
        
        SaveService.saveTree(hashTree, new FileOutputStream(new File(filename)));
    }

    public void startRecording() throws IOException
    {
        ctrl.startProxy();
    }

    public void stopRecording()
    {
        ctrl.stopProxy();
    }
}
