package com.focusit.jmeter;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.collections.HashTree;

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

    private static final GroovyShell shell = new GroovyShell();

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
        JMeterScriptProcessor.compiledRecordingScript = shell.parse(recordingScript);
    }

    public static String getProcessScript() {
        return processScript;
    }

    public static void setProcessScript(String processScript) {
        JMeterScriptProcessor.processScript = processScript;
        JMeterScriptProcessor.compiledProcessScript = shell.parse(processScript);
    }

    /**
     * Post process sample with groovy script.
     *
     * @param sampler
     * @param result
     * @return is sample ok
     */
    public boolean processSampleDuringRecord(HTTPSamplerBase sampler, SampleResult result) {
        Binding binding = new Binding();
        binding.setVariable("request", sampler);
        binding.setVariable("response", result);

        compiledRecordingScript.setBinding(binding);
        boolean isOk = (Boolean) compiledRecordingScript.run();
        return isOk;
    }

    /**
     * Post process every stored request just before it get saved to disk
     * @param sample  recorded http-request (sample)
     * @param tree HashTree (XML like data structure) that represents exact recorded sample
     */
    public void processScenario(HTTPSamplerBase sample, HashTree tree) {
        Binding binding = new Binding();
        binding.setVariable("sample", sample);
        binding.setVariable("tree", tree);

        compiledProcessScript.setBinding(binding);
        compiledProcessScript.run();
    }

}
