package com.focusit.jsflight.player.context;

import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import com.focusit.jmeter.JMeterJSFlightBridge;

import groovy.lang.Binding;

public class PlayerContext
{
    private static final ConcurrentHashMap<String, Object> context = new ConcurrentHashMap<>();
    private static final PlayerContext instance = new PlayerContext();

    public static PlayerContext getInstance()
    {
        return instance;
    }

    public Binding asBindings()
    {
        Binding binding = new Binding(context);
        return binding;
    }

    public Object get(String key)
    {
        return context.get(key);
    }

    public JSONObject getCurrentScenarioStep()
    {
        return JMeterJSFlightBridge.getInstace().getCurrentScenarioStep();
    }

    public void put(String key, Object value)
    {
        context.put(key, value);
    }

    public void reset()
    {
        context.keySet().forEach(it -> {
            context.remove(it);
        });
    }

    public void setCurrentScenarioStep(JSONObject currentScenarioStep)
    {
        JMeterJSFlightBridge.getInstace().setCurrentScenarioStep(currentScenarioStep);
    }

}
