package com.focusit.jmeter;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.json.JSONObject;

public class JMeterJSFlightBridge
{
    private static final JMeterJSFlightBridge instance = new JMeterJSFlightBridge();

    public static JMeterJSFlightBridge getInstace()
    {
        return instance;
    }

    private final ConcurrentHashMap<HTTPSamplerBase, JSONObject> samplersEvents = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, JSONObject> namesEvents = new ConcurrentHashMap<>();

    private JSONObject currentScenarioStep = null;

    public static String TAG_FIELD = "uuid";

    private JMeterJSFlightBridge()
    {

    }

    public void addSampler(HTTPSamplerBase sampler)
    {
        namesEvents.put(sampler.getName(), getCurrentScenarioStep());
        samplersEvents.put(sampler, getCurrentScenarioStep());
    }

    public JSONObject getCurrentScenarioStep()
    {
        return currentScenarioStep;
    }

    public JSONObject getSourceEvent(HTTPSamplerBase sampler)
    {
        return samplersEvents.get(sampler);
    }

    public void setCurrentScenarioStep(JSONObject currentScenarioStep)
    {
        this.currentScenarioStep = currentScenarioStep;
    }
}
