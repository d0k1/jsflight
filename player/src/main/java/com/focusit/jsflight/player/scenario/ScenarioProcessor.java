package com.focusit.jsflight.player.scenario;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.focusit.jsflight.player.constants.EventType;
import com.focusit.jsflight.player.script.PlayerScriptProcessor;
import com.focusit.jsflight.player.webdriver.SeleniumDriver;

/**
 * Class that really replays an event in given scenario and given selenium driver
 * Created by doki on 05.05.16.
 */
public class ScenarioProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger(ScenarioProcessor.class);

    private static WebDriver getWebDriver(UserScenario scenario, SeleniumDriver seleniumDriver, JSONObject event)
    {
        boolean firefox = scenario.getConfiguration().getCommonConfiguration().isUseFirefox();
        String path = firefox ? scenario.getConfiguration().getCommonConfiguration().getFfPath()
                : scenario.getConfiguration().getCommonConfiguration().getPjsPath();
        String proxyHost = scenario.getConfiguration().getCommonConfiguration().getProxyHost();
        String proxyPort = scenario.getConfiguration().getCommonConfiguration().getProxyPort();
        String display = scenario.getConfiguration().getCommonConfiguration().getFirefoxDisplay();
        WebDriver theWebDriver = seleniumDriver.getDriverForEvent(event, firefox, path, display, proxyHost, proxyPort);

        return theWebDriver;
    }

    /**
     * Method that check if a browser has error dialog visible.
     * aand if it has then throws an exception.
     * A browser after any step should not contain any error
     * @param scenario
     * @param wd
     * @throws Exception
     */
    protected void hasBrowserAnError(UserScenario scenario, WebDriver wd) throws Exception
    {
        try
        {
            Object result = new PlayerScriptProcessor().executeWebLookupScript(
                    scenario.getConfiguration().getWebConfiguration().getFindBrowserErrorScript(), wd, null, null);
            if (Boolean.parseBoolean(result.toString()))
            {
                throw new IllegalStateException("Browser contains some error after step processing");
            }
        }
        catch (Exception e)
        {
            LOG.error(e.toString(), e);
        }
    }

    /**
     * This method will decide whether step processing should be terminatedd at current step or not.
     * Or, in other words, should an exception be there or not.
     * Default implementation just logs
     * @param position
     * @param ex
     * @throws Exception
     */
    protected void processClickExcpetion(int position, Exception ex) throws Exception
    {
        LOG.error("Failed to process step: " + position, ex);
    }

    /**
     * Make a screenshot and save to a file
     * @param scenario
     * @param seleniumDriver
     * @param theWebDriver
     * @param position
     */
    protected void makeAShot(UserScenario scenario, SeleniumDriver seleniumDriver, WebDriver theWebDriver, int position)
    {
        if (scenario.getConfiguration().getCommonConfiguration().getMakeShots())
        {
            String screenDir = scenario.getConfiguration().getCommonConfiguration().getScreenDir();
            File dir = new File(
                    screenDir + File.separator + Paths.get(scenario.getScenarioFilename()).getFileName().toString());

            if (!dir.exists() && !dir.mkdirs())
            {
                return;
            }

            try (FileOutputStream fos = new FileOutputStream(
                    new String(dir.getAbsolutePath() + File.separator + String.format("%05d", position) + ".png")))
            {
                seleniumDriver.makeAShot(theWebDriver, fos);
            }
            catch (IOException e)
            {
                LOG.error(e.toString(), e);
            }
        }
    }

    public void applyStep(UserScenario scenario, SeleniumDriver seleniumDriver, int position)
    {
        scenario.getContext().setCurrentScenarioStep(scenario.getStepAt(position));

        new PlayerScriptProcessor().runStepPrePostScript(scenario, position, true);
        JSONObject event = scenario.getStepAt(position);
        event = new PlayerScriptProcessor().runStepTemplating(scenario, event);
        boolean error = false;
        try
        {
            if (scenario.isStepDuplicates(scenario.getConfiguration().getWebConfiguration().getDuplicationScript(),
                    event))
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
                new PlayerScriptProcessor().executeScriptEvent(
                        scenario.getConfiguration().getScriptEventConfiguration().getScript(), event);
                return;
            }

            WebDriver theWebDriver = getWebDriver(scenario, seleniumDriver, event);

            int pageTimeoutMs = Integer
                    .parseInt(scenario.getConfiguration().getCommonConfiguration().getPageReadyTimeout());
            String checkPageJs = scenario.getConfiguration().getCommonConfiguration().getCheckPageJs();
            String maxElementGroovy = scenario.getConfiguration().getCommonConfiguration().getMaxElementGroovy();
            String lookupScript = scenario.getConfiguration().getWebConfiguration().getLookupScript();
            String uiShownScript = scenario.getConfiguration().getCommonConfiguration().getUiShownScript();

            seleniumDriver.openEventUrl(theWebDriver, event, pageTimeoutMs,
                    scenario.getConfiguration().getCommonConfiguration().getCheckPageJs(), uiShownScript);

            WebElement element = null;

            String target = scenario.getTargetForEvent(event);

            LOG.info("Event type: {}", type);
            seleniumDriver.waitPageReady(theWebDriver, event, pageTimeoutMs,
                    scenario.getConfiguration().getCommonConfiguration().getCheckPageJs());

            try
            {
                switch (type)
                {
                case EventType.MOUSEWHEEL:
                    seleniumDriver.processMouseWheel(lookupScript, theWebDriver, event, target);
                    break;
                case EventType.SCROLL_EMULATION:
                    seleniumDriver.processScroll(theWebDriver, event, target, pageTimeoutMs, checkPageJs,
                            maxElementGroovy);
                    break;

                case EventType.MOUSEDOWN:
                case EventType.CLICK:
                    element = seleniumDriver.findTargetWebElement(lookupScript, theWebDriver, event, target);
                    seleniumDriver.processMouseEvent(theWebDriver, event, element);
                    seleniumDriver.waitPageReady(theWebDriver, event, pageTimeoutMs, checkPageJs);
                    break;
                case EventType.KEY_UP:
                case EventType.KEY_DOWN:
                case EventType.KEY_PRESS:
                    element = seleniumDriver.findTargetWebElement(lookupScript, theWebDriver, event, target);
                    seleniumDriver.processKeyboardEvent(theWebDriver, event, element,
                            scenario.getConfiguration().getCommonConfiguration().isUseRandomChars());
                    seleniumDriver.waitPageReady(theWebDriver, event, pageTimeoutMs, checkPageJs);
                    break;
                default:
                    break;
                }

                makeAShot(scenario, seleniumDriver, theWebDriver, position);

                hasBrowserAnError(scenario, theWebDriver);
            }
            catch (Exception e)
            {
                processClickExcpetion(position, e);
            }

            seleniumDriver.releaseBrowser(theWebDriver,
                    scenario.getConfiguration().getCommonConfiguration().getFormOrDialogXpath());
        }
        catch (Exception e)
        {
            error = true;
            throw new RuntimeException(e);
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

    public void play(UserScenario scenario, SeleniumDriver seleniumDriver)
    {
        long begin = System.currentTimeMillis();

        LOG.info("playing the scenario");

        while (scenario.getPosition() < scenario.getStepsCount())
        {
            if (scenario.getPosition() > 0)
            {
                LOG.info("Step " + scenario.getPosition());
            }
            else
            {
                LOG.info("Step 0");
            }
            applyStep(scenario, seleniumDriver, scenario.getPosition());
            if (scenario.getPosition() + 1 < scenario.getStepsCount())
            {
                scenario.next();
            }
            else
            {
                break;
            }
        }
        scenario.next();
        LOG.info(String.format("Done(%d):playing", System.currentTimeMillis() - begin));
        seleniumDriver.closeWebDrivers();
    }

    public void play(UserScenario scenario, SeleniumDriver seleniumDriver, int start, int finish)
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
                LOG.info("Step " + scenario.getPosition());
            }
            else
            {
                LOG.info("Step 0");
            }
            applyStep(scenario, seleniumDriver, scenario.getPosition());
            if (scenario.getPosition() + 1 < maxPosition)
            {
                scenario.next();
            }
            else
            {
                break;
            }
        }
        scenario.next();
        LOG.info(String.format("Done(%d):playing", System.currentTimeMillis() - begin));
        seleniumDriver.closeWebDrivers();
    }

}
