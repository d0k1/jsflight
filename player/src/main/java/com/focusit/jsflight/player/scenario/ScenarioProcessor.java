package com.focusit.jsflight.player.scenario;

import com.focusit.jsflight.player.constants.EventType;
import com.focusit.jsflight.player.script.PlayerScriptProcessor;
import com.focusit.jsflight.player.webdriver.SeleniumDriver;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that really replays an event in given scenario and given selenium driver
 * Created by doki on 05.05.16.
 */
public class ScenarioProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(ScenarioProcessor.class);

    private static WebDriver getWebDriver(UserScenario scenario, SeleniumDriver seleniumDriver, JSONObject event){
        boolean firefox = scenario.getConfiguration().getCommonConfiguration().isUseFirefox();
        String path = firefox ? scenario.getConfiguration().getCommonConfiguration().getFfPath() : scenario.getConfiguration().getCommonConfiguration().getPjsPath();
        String proxyHost = scenario.getConfiguration().getCommonConfiguration().getProxyHost();
        String proxyPort = scenario.getConfiguration().getCommonConfiguration().getProxyPort();
        String display = scenario.getConfiguration().getCommonConfiguration().getFirefoxDisplay();
        WebDriver theWebDriver = seleniumDriver.getDriverForEvent(event, firefox, path, display, proxyHost, proxyPort);

        return theWebDriver;
    }

    public static void applyStep(UserScenario scenario, SeleniumDriver seleniumDriver, int position){
        scenario.getContext().setCurrentScenarioStep(scenario.getStepAt(position));

        new PlayerScriptProcessor().runStepPrePostScript(scenario, position, true);
        JSONObject event = scenario.getStepAt(position);
        event = new PlayerScriptProcessor().runStepTemplating(scenario, event);
        boolean error = false;
        try
        {
            if (scenario.isStepDuplicates(event))
            {
                LOG.warn("Event duplicates prev");
                return;
            }
            String eventType = event.getString("type");

            if (scenario.isEventIgnored(eventType) || scenario.isEventBad(event))
            {
                LOG.warn("Event is ignored or bad");
                return;
            }

            String type = event.getString("type");

            if (type.equalsIgnoreCase(EventType.SCRIPT))
            {
                new PlayerScriptProcessor().executeScriptEvent(event);
                return;
            }

            WebDriver theWebDriver = getWebDriver(scenario, seleniumDriver, event);
            int pageTimeoutMs = Integer.parseInt(scenario.getConfiguration().getCommonConfiguration().getPageReadyTimeout());

            seleniumDriver.openEventUrl(theWebDriver, event, pageTimeoutMs);

            WebElement element = null;

            String target = scenario.getTargetForEvent(event);

            LOG.info("Event type: {}", type);
            seleniumDriver.waitPageReady(theWebDriver, event, pageTimeoutMs);

            try
            {
                switch (type)
                {

                    case EventType.MOUSEWHEEL:
                        seleniumDriver.processMouseWheel(theWebDriver, event, target);
                        break;
                    case EventType.SCROLL_EMULATION:
                        seleniumDriver.processScroll(theWebDriver, event, target, pageTimeoutMs);
                        break;

                    case EventType.MOUSEDOWN:
                    case EventType.CLICK:
                        element = seleniumDriver.findTargetWebElement(theWebDriver, event, target);
                        seleniumDriver.processMouseEvent(theWebDriver, event, element);
                        seleniumDriver.waitPageReady(theWebDriver, event, pageTimeoutMs);
                        break;
                    case EventType.KEY_UP:
                    case EventType.KEY_DOWN:
                    case EventType.KEY_PRESS:
                        element = seleniumDriver.findTargetWebElement(theWebDriver, event, target);
                        seleniumDriver.processKeyboardEvent(theWebDriver, event, element, scenario.getConfiguration().getCommonConfiguration().isUseRandomChars());
                        seleniumDriver.waitPageReady(theWebDriver, event, pageTimeoutMs);
                        break;
                    default:
                        break;
                }
            }
            catch (Exception e)
            {
                LOG.error("Failed to process step: " + position, e);
            }

            if(scenario.getConfiguration().getCommonConfiguration().getMakeShots()) {
                seleniumDriver.makeAShot(theWebDriver, scenario.getConfiguration().getCommonConfiguration().getScreenDir());
            }
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
                scenario.updatePrevEvent(event);
                new PlayerScriptProcessor().runStepPrePostScript(scenario, position, false);
            }
        }
    }

    public static void play(UserScenario scenario, SeleniumDriver seleniumDriver)
    {
        long begin = System.currentTimeMillis();

        LOG.info("playing the scenario");

        while (scenario.getPosition() < scenario.getStepsCount())
        {
            if (scenario.getPosition() > 0)
            {
                JSONObject event = scenario.getStepAt(scenario.getPosition());
                WebDriver theWebDriver = getWebDriver(scenario, seleniumDriver, event);
                int pageTimeoutMs = Integer.parseInt(scenario.getConfiguration().getCommonConfiguration().getPageReadyTimeout());

                seleniumDriver.waitPageReady(theWebDriver, event, pageTimeoutMs);
                LOG.info("Step " + scenario.getPosition());
            }
            else
            {
                LOG.info("Step 0");
            }
            applyStep(scenario, seleniumDriver, scenario.getPosition());
            if(scenario.getPosition()+1<scenario.getStepsCount()) {
                scenario.next();
            } else {
                break;
            }

        }
        scenario.next();
        LOG.info(String.format("Done(%d):playing", System.currentTimeMillis() - begin));
    }

    public static void play(UserScenario scenario, SeleniumDriver seleniumDriver, int start, int finish)
    {
        long begin = System.currentTimeMillis();

        LOG.info("playing the scenario");
        if (start > 0)
        {
            LOG.info("skiping " + start + " events.");
            scenario.setPosition(start);
        }

        int maxPosition = scenario.getStepsCount();

        if (finish > 0)
        {
            maxPosition = finish;
        }

        while (scenario.getPosition() < maxPosition)
        {
            if (scenario.getPosition() > 0)
            {
                JSONObject event = scenario.getStepAt(scenario.getPosition());
                WebDriver theWebDriver = getWebDriver(scenario, seleniumDriver, event);
                int pageTimeoutMs = Integer.parseInt(scenario.getConfiguration().getCommonConfiguration().getPageReadyTimeout());

                seleniumDriver.waitPageReady(theWebDriver, event, pageTimeoutMs);
                LOG.info("Step " + scenario.getPosition());
            }
            else
            {
                LOG.info("Step 0");
            }
            applyStep(scenario, seleniumDriver, scenario.getPosition());
            if(scenario.getPosition()+1<maxPosition) {
                scenario.next();
            } else {
                break;
            }
        }
        scenario.next();
        LOG.info(String.format("Done(%d):playing", System.currentTimeMillis() - begin));
    }

}
