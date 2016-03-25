package com.focusit.jsflight.player.controller;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class JMeterController extends UIController
{
    private static final long serialVersionUID = 1L;
    private final static JMeterController instance = new JMeterController();
    private String stepProcessorScript = "";
    private String scenarioProcessorScript = "";

    public static JMeterController getInstance()
    {
        return instance;
    }

    private JMeterController()
    {
    }

    @Override
    public void load(String file) throws Exception
    {
        ObjectInputStream stream = getInputStream(file);
        stepProcessorScript = (String) stream.readObject();
        scenarioProcessorScript = (String) stream.readObject();
    }

    @Override
    public void store(String file) throws Exception
    {
        ObjectOutputStream stream = getOutputStream(file);
        stream.writeObject(stepProcessorScript);
        stream.writeObject(scenarioProcessorScript);
    }

    @Override
    protected String getControllerDataFilename()
    {
        return null;
    }

    public String getStepProcessorScript() {
        return stepProcessorScript;
    }

    public void setStepProcessorScript(String stepProcessorScript) {
        this.stepProcessorScript = stepProcessorScript;
    }

    public String getScenarioProcessorScript() {
        return scenarioProcessorScript;
    }

    public void setScenarioProcessorScript(String scenarioProcessorScript) {
        this.scenarioProcessorScript = scenarioProcessorScript;
    }
}
