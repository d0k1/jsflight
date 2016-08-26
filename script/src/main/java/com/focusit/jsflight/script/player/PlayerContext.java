package com.focusit.jsflight.script.player;

import com.focusit.jsflight.script.jmeter.JMeterJSFlightBridge;
import groovy.lang.Binding;
import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Some intermediate variables passed between various scripts and other modules
 */
public class PlayerContext
{
    private static final ConcurrentHashMap<String, Object> context = new ConcurrentHashMap<>();

    public Binding asBindings()
    {
        return new Binding(context);
    }

    public Object get(String key)
    {
        return context.get(key);
    }

    public void setCurrentScenarioStep(JSONObject currentScenarioStep)
    {
        JMeterJSFlightBridge.getInstance().setCurrentScenarioStep(currentScenarioStep);
    }

    public void put(String key, Object value)
    {
        context.put(key, value);
    }

    public void reset()
    {
        context.keySet().forEach(context::remove);
    }
}
