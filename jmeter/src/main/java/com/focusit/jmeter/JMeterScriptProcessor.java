package com.focusit.jmeter;

import com.focusit.script.ScriptEngine;
import com.focusit.script.constants.ScriptBindingConstants;
import groovy.lang.Binding;
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
public class JMeterScriptProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger(JMeterScriptProcessor.class);
    // script called at recording phase. Can skip sample
    private String recordingScript;
    // script callled at storing phase. Can skip sample
    private String processScript;
    private final ClassLoader classLoader;

    public JMeterScriptProcessor()
    {
        this.classLoader = ScriptEngine.getClassLoader();
    }

    public String getRecordingScript()
    {
        return recordingScript;
    }

    public void setRecordingScript(String recordingScript)
    {
        this.recordingScript = recordingScript;
    }

    public String getProcessScript()
    {
        return processScript;
    }

    public void setProcessScript(String processScript)
    {
        this.processScript = processScript;
    }

    /**
     * Post process sample with groovy script.
     *
     * @param sampler
     * @param result
     * @param recorder
     * @return is sample ok
     */
    public boolean processSampleDuringRecord(HTTPSamplerBase sampler, SampleResult result, JMeterRecorder recorder)
    {
        Binding binding = new Binding();
        binding.setVariable(ScriptBindingConstants.LOGGER, LOG);
        binding.setVariable(ScriptBindingConstants.REQUEST, sampler);
        binding.setVariable(ScriptBindingConstants.RESPONSE, result);
        binding.setVariable(ScriptBindingConstants.CONTEXT, recorder.getContext());
        binding.setVariable(ScriptBindingConstants.JSFLIGHT, recorder.getBridge());
        binding.setVariable(ScriptBindingConstants.CLASSLOADER, classLoader);

        boolean isOk = true;

        Script s = ScriptEngine.getScript(recordingScript);
        if (s == null)
        {
            LOG.warn("Sample " + sampler.getName() + ". No script found. default result " + isOk);
            return isOk;
        }
        s.setBinding(binding);
        LOG.info("Running " + sampler.getName() + " compiled script");
        Object scriptResult = s.run();

        if (scriptResult != null && scriptResult instanceof Boolean)
        {
            isOk = (boolean)scriptResult;
        }
        else
        {
            LOG.warn("Sample " + sampler.getName() + " script result UNDEFINED shifted to" + isOk);
        }

        LOG.info("Sample " + sampler.getName() + " script result " + isOk);
        return isOk;
    }

    /**
     * Post process every stored request just before it get saved to disk
     *
     * @param sample recorded http-request (sample)
     * @param tree   HashTree (XML like data structure) that represents exact recorded sample
     */
    public void processScenario(HTTPSamplerBase sample, HashTree tree, Arguments userVariables, JMeterRecorder recorder)
    {
        Binding binding = new Binding();
        binding.setVariable(ScriptBindingConstants.LOGGER, LOG);
        binding.setVariable(ScriptBindingConstants.SAMPLE, sample);
        binding.setVariable(ScriptBindingConstants.TREE, tree);
        binding.setVariable(ScriptBindingConstants.CONTEXT, recorder.getContext());
        binding.setVariable(ScriptBindingConstants.JSFLIGHT, recorder.getBridge());
        binding.setVariable(ScriptBindingConstants.USER_VARIABLES, userVariables);
        binding.setVariable(ScriptBindingConstants.CLASSLOADER, classLoader);

        Script compiledProcessScript = ScriptEngine.getScript(processScript);
        if (compiledProcessScript == null)
        {
            return;
        }
        compiledProcessScript.setBinding(binding);
        compiledProcessScript.run();
    }
}
