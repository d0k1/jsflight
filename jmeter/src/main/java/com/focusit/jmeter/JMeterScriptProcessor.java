package com.focusit.jmeter;

import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jorphan.collections.HashTree;

/**
 * Created by dkirpichenkov on 25.03.16.
 */
public class JMeterScriptProcessor {

    /**
     * Post process sample with groovy script.
     *
     * @param sampler
     * @param result
     * @return is sample ok or should be skipped
     */
    public boolean processSampleDuringRecord(HTTPSamplerBase sampler, HTTPSampleResult result) {
        return true;
    }

    public void processScenario(HTTPSamplerBase sample, HashTree tree) {

    }

}
