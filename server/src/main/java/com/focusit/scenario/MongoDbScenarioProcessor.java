package com.focusit.scenario;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.focusit.jsflight.player.scenario.ScenarioProcessor;
import com.focusit.jsflight.player.scenario.UserScenario;
import com.focusit.jsflight.player.script.PlayerScriptProcessor;
import com.focusit.jsflight.player.webdriver.SeleniumDriver;
import com.focusit.player.ErrorInBrowserPlaybackException;
import com.focusit.service.MongoDbStorageService;

/**
 * Created by doki on 12.05.16.
 */
public class MongoDbScenarioProcessor extends ScenarioProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger(MongoDbScenarioProcessor.class);
    private MongoDbStorageService screenshotsService;

    public MongoDbScenarioProcessor(MongoDbStorageService screenshotsService)
    {
        this.screenshotsService = screenshotsService;
    }

    @Override
    protected void hasBrowserAnError(UserScenario scenario, WebDriver wd) throws Exception
    {
        try
        {
            Object result = new PlayerScriptProcessor().executeWebLookupScript(
                    scenario.getConfiguration().getWebConfiguration().getFindBrowserErrorScript(), wd, null, null);
            if (Boolean.parseBoolean(result.toString()))
            {
                throw new ErrorInBrowserPlaybackException("Browser contains some error after step processing");
            }
        }
        catch (Exception e)
        {
            LOG.debug(e.toString(), e);
        }
    }

    @Override
    public void applyStep(UserScenario scenario, SeleniumDriver seleniumDriver, int position)
    {
        LOG.info("Applying event: " + scenario.getStepAt(position).get("eventId"));
        super.applyStep(scenario, seleniumDriver, position);
    }

    @Override
    protected void processClickExcpetion(int position, Exception ex) throws Exception
    {
        super.processClickExcpetion(position, ex);
        throw ex;
    }

    @Override
    protected void makeAShot(UserScenario scenario, SeleniumDriver seleniumDriver, WebDriver theWebDriver, int position)
    {
        MongoDbScenario mongoDbScenario = (MongoDbScenario)scenario;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            seleniumDriver.makeAShot(theWebDriver, baos);

            try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray()))
            {
                screenshotsService.storeScreenshot(mongoDbScenario, position, bais);
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
