package com.focusit.jmeter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Context to hold a http sample pre/post processing context.
 * Helps to modify requests with JMeter's context variables
 * Created by doki on 25.03.16.
 */
public class JMeterRecorderContext {
    private final static JMeterRecorderContext instance = new JMeterRecorderContext();

    private static final ConcurrentHashMap<String, String> templateReplacemnts = new ConcurrentHashMap<>();

    private JMeterRecorderContext(){

    }

    public static JMeterRecorderContext getInstance(){
        return instance;
    }

    public void addTemplate(String source, String template){
        templateReplacemnts.put(source, template);
    }

    public String getTemplate(String source) {
        return templateReplacemnts.get(source);
    }
}
