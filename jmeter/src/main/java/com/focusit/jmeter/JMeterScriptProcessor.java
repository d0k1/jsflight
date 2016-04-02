package com.focusit.jmeter;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to run groovy scripts against recorded samples
 * Created by doki on 25.03.16.
 */
public class JMeterScriptProcessor {
    private final static JMeterScriptProcessor instance = new JMeterScriptProcessor();

    // script called at recording phase. Can skip sample
    private static String recordingScript;
    // script callled at storing phase. Can skip sample
    private static String processScript;

    // And compiled version of that scripts.
    private static Script compiledRecordingScript;

    private static Script compiledProcessScript;

    private static final GroovyShell shell = new GroovyShell(new ScriptsClassLoader(JMeterScriptProcessor.class.getClassLoader()));

    private static final Logger log = LoggerFactory.getLogger(JMeterScriptProcessor.class);

    private JMeterScriptProcessor(){

    }

    public static JMeterScriptProcessor getInstance(){
        return instance;
    }

    public static String getRecordingScript() {
        return recordingScript;
    }

    public static void setRecordingScript(String recordingScript) {
        JMeterScriptProcessor.recordingScript = recordingScript;
        try {
            JMeterScriptProcessor.compiledRecordingScript = shell.parse(recordingScript);
        } catch (Exception ex) {
            log.error(ex.toString(), ex);
        }
    }

    public static String getProcessScript() {
        return processScript;
    }

    public static void setProcessScript(String processScript) {
        JMeterScriptProcessor.processScript = processScript;
        try {
            JMeterScriptProcessor.compiledProcessScript = shell.parse(processScript);
        } catch (Exception ex) {
            log.error(ex.toString(), ex);
        }
    }

    /**
     * Post process sample with groovy script.
     *
     * @param sampler
     * @param result
     * @return is sample ok
     */
    public boolean processSampleDuringRecord(HTTPSamplerBase sampler, SampleResult result) {
        if(compiledRecordingScript==null) {
            return true;
        }

        Binding binding = new Binding();
        binding.setVariable("request", sampler);
        binding.setVariable("response", result);
        binding.setVariable("ctx", JMeterRecorderContext.getInstance());
        binding.setVariable("jsflight", JMeterJSFlightBridge.getInstace());
        binding.setVariable("classloader", shell.getClassLoader());

        compiledRecordingScript.setBinding(binding);
        boolean isOk = true;
        Object scriptResult = compiledRecordingScript.run();

        if(scriptResult!=null && scriptResult instanceof Boolean){
            isOk = (boolean) scriptResult;
        }

        return isOk;
    }

    /**
     * Post process every stored request just before it get saved to disk
     * @param sample  recorded http-request (sample)
     * @param tree HashTree (XML like data structure) that represents exact recorded sample
     */
    public void processScenario(HTTPSamplerBase sample, HashTree tree, Arguments userVariables) {
        if (compiledProcessScript==null) {
            return;
        }

        Binding binding = new Binding();
        binding.setVariable("sample", sample);
        binding.setVariable("tree", tree);
        binding.setVariable("ctx", JMeterRecorderContext.getInstance());
        binding.setVariable("jsflight", JMeterJSFlightBridge.getInstace());
        binding.setVariable("vars", userVariables);
        binding.setVariable("classloader", shell.getClassLoader());

        compiledProcessScript.setBinding(binding);
        compiledProcessScript.run();
    }

}
