package com.focusit.jsflight.player.controller;

import com.focusit.jmeter.JMeterRecorder;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class JMeterController extends UIController
{
    private static final long serialVersionUID = 1L;
    private final static JMeterController instance = new JMeterController();
    private String stepProcessorScript = "";
    private String scenarioProcessorScript = "";

    private JMeterController()
    {
    }

    public static JMeterController getInstance()
    {
        return instance;
    }

    @Override
    public void load(String file) throws Exception
    {
        ObjectInputStream stream = getInputStream(file);
        stepProcessorScript = (String) stream.readObject();
        scenarioProcessorScript = (String) stream.readObject();
        //syncScripts();
    }

    public void syncScripts(JMeterRecorder recorder){
        if(getStepProcessorScript()!=null) {
            recorder.getScriptProcessor().setRecordingScript(getStepProcessorScript());
        }

        if(getScenarioProcessorScript()!=null) {
            recorder.getScriptProcessor().setProcessScript(getScenarioProcessorScript());
        }
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

    public void setStepProcessorScript(JMeterRecorder recorder, String stepProcessorScript) {
        this.stepProcessorScript = stepProcessorScript;
        syncScripts(recorder);
    }

    public String getScenarioProcessorScript() {
        return scenarioProcessorScript;
    }

    public void setScenarioProcessorScript(JMeterRecorder recorder, String scenarioProcessorScript) {
        this.scenarioProcessorScript = scenarioProcessorScript;
        syncScripts(recorder);
    }
}
