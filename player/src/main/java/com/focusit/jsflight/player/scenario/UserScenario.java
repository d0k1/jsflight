package com.focusit.jsflight.player.scenario;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.focusit.jsflight.player.constants.EventType;
import com.focusit.jsflight.player.context.PlayerContext;
import com.focusit.jsflight.player.input.Events;
import com.focusit.jsflight.player.input.FileInput;
import com.focusit.jsflight.player.script.Engine;
import com.focusit.jsflight.player.webdriver.SeleniumDriver;

/**
 * Recorded scenario encapsulation: parses file, plays the scenario by step, modifies the scenario, saves to a disk
 * 
 * @author Denis V. Kirpichenkov
 *
 */
public class UserScenario
{
    private static String TAG_FIELD = "uuid";
    private static volatile int position = 0;

    private static final Logger log = LoggerFactory.getLogger(UserScenario.class);

    private static List<JSONObject> events = new ArrayList<>();

    private static String postProcessScenarioScript = "";

    private static List<Boolean> checks = new ArrayList<>();

    private static HashMap<String, JSONObject> lastEvents = new HashMap<>();

    public static int getPosition()
    {
        return position;
    }

    public static String getPostProcessScenarioScript()
    {
        return postProcessScenarioScript;
    }

    public static String getScenarioFilename()
    {
        return "";
    }

    public static int getStepsCount()
    {
        return events.size();
    }

    public static String getTagForEvent(JSONObject event)
    {
        String tag = "null";
        if (event.has(TAG_FIELD))
        {
            tag = event.getString(TAG_FIELD);
        }

        return tag;
    }

    public static void setPostProcessScenarioScript(String postProcessScenarioScript)
    {
        UserScenario.postProcessScenarioScript = postProcessScenarioScript;
    }

    public static void updatePrevEvent(JSONObject event)
    {
        lastEvents.put(getTagForEvent(event), event);
    }

    public void applyStep(int position)
    {
        new Engine().runStepPrePostScript(this, position, true);
        JSONObject event = events.get(position);
        event = new Engine().runStepTemplating(event);
        boolean error = false;
        try
        {
            if (isStepDuplicates(event))
            {
                return;
            }
            String eventType = event.getString("type");

            if (isEventIgnored(eventType) || isEventBad(event))
            {
                return;
            }

            SeleniumDriver.openEventUrl(event);

            WebElement element = null;

            String target = getTargetForEvent(event);

            String type = event.getString("type");

            SeleniumDriver.waitPageReady(event);

            switch (type)
            {
            case EventType.SCROLL_EMULATION:
                SeleniumDriver.processScroll(event, target);
                break;

            case EventType.MOUSEDOWN:
                element = SeleniumDriver.findTargetWebElement(event, target);
                SeleniumDriver.processMouseEvent(event, element);
                SeleniumDriver.waitPageReady(event);
                break;

            case EventType.KEY_UP:
            case EventType.KEY_PRESS:
                element = SeleniumDriver.findTargetWebElement(event, target);

                SeleniumDriver.processKeyboardEvent(event, element);

                SeleniumDriver.waitPageReady(event);
                break;
            default:
                break;
            }

            SeleniumDriver.makeAShot(event);
        }
        catch (Exception e)
        {
            error = true;
            throw e;
        }
        finally
        {
            if (!error)
            {
                updatePrevEvent(event);
                new Engine().runStepPrePostScript(this, position, false);
            }
        }
    }

    public void checkStep(int position)
    {

    }

    public void copyStep(int position)
    {
        String event = events.get(position).toString();
        JSONObject clone = new JSONObject(event);
        events.add(position, clone);
    }

    public void deleteStep(int position)
    {
        events.remove(position);
    }

    public List<Boolean> getChecks()
    {
        return checks;
    }

    public JSONObject getPrevEvent(JSONObject event)
    {
        return lastEvents.get(getTagForEvent(event));
    }

    public JSONObject getStepAt(int position)
    {
        return events.get(position);
    }

    public String getTargetForEvent(JSONObject event)
    {
        if (!event.has("target1"))
        {
            return "";
        }
        JSONArray array = event.getJSONArray("target1");
        if (array.isNull(0))
        {
            return "";
        }

        String target = array.getJSONObject(0).getString("getxp");
        return target;
    }

    public boolean isStepDuplicates(JSONObject event)
    {
        JSONObject prev = getPrevEvent(event);
        if (prev == null)
        {
            return false;
        }

        if (prev.getString("type").equals(event.getString("type"))
                && prev.getString("url").equals(event.getString("url"))
                && getTargetForEvent(prev).equals(getTargetForEvent(event)))
        {
            return true;
        }

        return false;
    }

    public void next()
    {
        position++;
        if (position == events.size())
        {
            for (int i = 0; i < position; i++)
            {
                checks.set(i, false);
            }
            position = 0;
        }
    }

    public void parse(String filename) throws IOException
    {
        events.clear();
        events.addAll(new Events().parse(FileInput.getContent(filename)));
        PlayerContext.getInstance().reset();
    }

    public void play()
    {
        long begin = System.currentTimeMillis();

        log.info("playing the scenario");

        while (position < events.size())
        {
            if (position > 0)
            {
                SeleniumDriver.waitPageReady(events.get(position));
                log.info("Step " + position);
            }
            else
            {
                log.info("Step 0");
            }
            applyStep(position);
            checks.set(position, true);
            position++;
            if (position == events.size())
            {
                for (int i = 0; i < position; i++)
                {
                    checks.set(i, false);
                }
            }
        }
        log.info(String.format("Done(%d):playing", System.currentTimeMillis() - begin));
        position--;
    }

    public long postProcessScenario()
    {
        if (!postProcessScenarioScript.isEmpty())
        {
            new Engine(postProcessScenarioScript).postProcessScenario(events);
        }
        checks = new ArrayList<>(events.size());
        for (int i = 0; i < events.size(); i++)
        {
            checks.add(new Boolean(false));
        }

        long secs = 0;

        if (events.size() > 0)
        {
            secs = events.get(events.size() - 1).getBigDecimal("timestamp").longValue()
                    - events.get(0).getBigDecimal("timestamp").longValue();
        }

        return secs;
    }

    public void postProcessStep()
    {

    }

    public void preprocessStep()
    {

    }

    public void prev()
    {
        if (position > 0)
        {
            position--;
        }
    }

    public void rewind()
    {
        checks.stream().forEach(it -> {
            it = Boolean.FALSE;
        });
        PlayerContext.getInstance().reset();
        position = 0;
    }

    public void runPostProcessor(String script)
    {
        Engine engine = new Engine(script);
        engine.testPostProcess(events);
    }

    public void saveScenario(String filename) throws IOException
    {
        FileInput.saveEvents(events, filename);
    }

    public void setChecks(List<Boolean> checks)
    {
        UserScenario.checks = checks;
    }

    public void setLastEvent(JSONObject event)
    {
        lastEvents.put(getTagForEvent(event), event);
    }

    public void setPosition(int position)
    {
        UserScenario.position = position;
    }

    public void setRawevents(Events rawevents)
    {
    }

    public void skip()
    {
        position++;
    }

    public void updateStep(int position, JSONObject event)
    {
        events.set(position, event);
    }

    private boolean isEventBad(JSONObject event)
    {
        return !event.has("target") || event.get("target") == null || event.get("target") == JSONObject.NULL;
    }

    private boolean isEventIgnored(String eventType)
    {
        return eventType.equalsIgnoreCase(EventType.XHR) || eventType.equalsIgnoreCase(EventType.HASH_CHANGE)
                || (!eventType.equalsIgnoreCase(EventType.MOUSEDOWN) && !eventType.equalsIgnoreCase(EventType.KEY_PRESS)
                        && !eventType.equalsIgnoreCase(EventType.KEY_UP))
                        && !eventType.equalsIgnoreCase(EventType.SCROLL_EMULATION);
    }
}
