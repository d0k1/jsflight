package com.focusit.jmeter;

import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
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
    }

    public static String getProcessScript() {
        return processScript;
    }

    public static void setProcessScript(String processScript) {
        JMeterScriptProcessor.processScript = processScript;
    }

    /**
     * Post process sample with groovy script.
     *
     * @param sampler
     * @param result
     * @return is sample ok or should be skipped
     */
    public boolean processSampleDuringRecord(HTTPSamplerBase sampler, SampleResult result) {
        return true;
    }

    public void processScenario(HTTPSamplerBase sample, HashTree tree) {

    }

}
