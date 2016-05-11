package com.focusit.jsflight.player.config;

import com.focusit.jmeter.JMeterRecorder;

import javax.annotation.Nullable;

/**
 * Created by dkirpichenkov on 06.05.16.
 */
public class JMeterConfiguration {
    private String stepProcessorScript = "";
    private String scenarioProcessorScript = "";

    public String getStepProcessorScript() {
        return stepProcessorScript;
    }

    public void setStepProcessorScript(@Nullable JMeterRecorder recorder, String stepProcessorScript) {
        this.stepProcessorScript = stepProcessorScript;
        if(recorder!=null)
        {
            syncScripts(recorder);
        }
    }

    public String getScenarioProcessorScript() {
        return scenarioProcessorScript;
    }

    public void setScenarioProcessorScript(@Nullable JMeterRecorder recorder, String scenarioProcessorScript) {
        this.scenarioProcessorScript = scenarioProcessorScript;
        if(recorder!=null)
        {
            syncScripts(recorder);
        }
    }

    public void syncScripts(JMeterRecorder recorder){
        if(getStepProcessorScript()!=null) {
            recorder.getScriptProcessor().setRecordingScript(getStepProcessorScript());
        }

        if(getScenarioProcessorScript()!=null) {
            recorder.getScriptProcessor().setProcessScript(getScenarioProcessorScript());
        }
    }
}
