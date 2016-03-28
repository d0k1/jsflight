package com.focusit.jmeter;

import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Class holds references to user scenario step and associated http samples.
 * Can be used during post processing of every http sample to find an appropriate cookie or other auth mechanism
 */
public class JMeterJSFlightBridge
{
    {
        NO_SCENARIO_STEP = new JSONObject();
        NO_SCENARIO_STEP.put("DO NOT USE IT", true);
    }

    public static JSONObject NO_SCENARIO_STEP;

    private static final JMeterJSFlightBridge instance = new JMeterJSFlightBridge();

    public static JMeterJSFlightBridge getInstace()
    {
        return instance;
    }

    private final ConcurrentHashMap<HTTPSamplerBase, JSONObject> samplersEvents = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, JSONObject> namesEvents = new ConcurrentHashMap<>();

    private JSONObject currentScenarioStep = NO_SCENARIO_STEP;

    public static String TAG_FIELD = "uuid";

    private JMeterJSFlightBridge()
    {

    }

    public void addSampler(HTTPSamplerBase sampler)
    {
        namesEvents.put(sampler.getName(), getCurrentScenarioStep());
        samplersEvents.put(sampler, getCurrentScenarioStep());
    }

    public boolean isCurrentStepEmpty(){
        return currentScenarioStep.equals(NO_SCENARIO_STEP);
    }

    public JSONObject getCurrentScenarioStep()
    {
        return currentScenarioStep;
    }

    public JSONObject getSourceEvent(HTTPSamplerBase sampler)
    {
    	JSONObject result = samplersEvents.get(sampler);
    	
    	if(result==null)
    	{
    		result = namesEvents.get(sampler.getName());
    	}
        
    	return result;
    }

    public void setCurrentScenarioStep(JSONObject currentScenarioStep)
    {
        this.currentScenarioStep = currentScenarioStep;
    }
}
