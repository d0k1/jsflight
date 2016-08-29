package com.focusit.jsflight.script.jmeter;

import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Class holds references to user scenario step and associated http samples.
 * Can be used during post processing of every http sample to find an appropriate cookie or other auth mechanism
 */
public final class JMeterJSFlightBridge
{
    public static final String DO_NOT_USE_IT_STEP = "DO NOT USE IT";
    public static final JSONObject NO_SCENARIO_STEP = new JSONObject().put(DO_NOT_USE_IT_STEP, true);
    private static final JMeterJSFlightBridge INSTANCE = new JMeterJSFlightBridge();

    private final ConcurrentHashMap<Object, JSONObject> samplersEvents = new ConcurrentHashMap<>();
    private JSONObject currentScenarioStep;

    private JMeterJSFlightBridge()
    {
        currentScenarioStep = NO_SCENARIO_STEP;
    }

    public static JMeterJSFlightBridge getInstance()
    {
        return INSTANCE;
    }

    public void addSampler(Object sampler)
    {
        samplersEvents.put(sampler, getCurrentScenarioStep());
    }

    public boolean isCurrentStepEmpty()
    {
        return currentScenarioStep.equals(NO_SCENARIO_STEP);
    }

    public JSONObject getCurrentScenarioStep()
    {
        return currentScenarioStep;
    }

    public void setCurrentScenarioStep(JSONObject currentScenarioStep)
    {
        this.currentScenarioStep = currentScenarioStep;
    }

    public JSONObject getSourceEvent(Object sampler)
    {
        return samplersEvents.get(sampler);
    }
}
