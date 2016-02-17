package com.focusit.jsflight.player.scenario;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.focusit.jsflight.player.input.Events;

public class UserScenario
{
    private static final String SET_ELEMENT_VISIBLE_JS = "var e = document.evaluate('%s' ,document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null ).singleNodeValue; if(e!== null) {e.style.visibility='visible';};";
    private List<JSONObject> events = new ArrayList<>();
    private List<Boolean> checks = new ArrayList<>();
    private int position = 0;

    public void applyStep(int position)
    {

    }

    public void checkStep(int position)
    {

    }

    public void copyStep(int position)
    {

    }

    public void deleteStep(int position)
    {

    }

    public List<Boolean> getChecks()
    {
        return checks;
    }

    public int getPosition()
    {
        return position;
    }

    public JSONObject getStepAt(int position)
    {
        return events.get(position);
    }

    public int getStepsCount()
    {
        return events.size();
    }

    public void next()
    {

    }

    public void parse(String filename)
    {

    }

    public void play()
    {

    }

    public void postProcessScenario()
    {

    }

    public void postProcessStep()
    {

    }

    public void preprocessStep()
    {

    }

    public void prev()
    {

    }

    public void rewind()
    {

    }

    public void runPostProcessor(String script)
    {

    }

    public void saveScenario(String filename) throws IOException
    {

    }

    public void setChecks(List<Boolean> checks)
    {
        this.checks = checks;
    }

    public void setPosition(int position)
    {
        this.position = position;
    }

    public void setRawevents(Events rawevents)
    {
    }

    public void skip()
    {

    }

    public void updateStep(int position, JSONObject event)
    {

    }
}
