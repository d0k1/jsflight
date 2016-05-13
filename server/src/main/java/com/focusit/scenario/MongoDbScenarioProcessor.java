package com.focusit.scenario;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.focusit.jsflight.player.scenario.ScenarioProcessor;
import com.focusit.jsflight.player.scenario.UserScenario;
import com.focusit.jsflight.player.webdriver.SeleniumDriver;
import com.focusit.service.ScreenshotsService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Created by doki on 12.05.16.
 */
public class MongoDbScenarioProcessor extends ScenarioProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(MongoDbScenarioProcessor.class);
    private ScreenshotsService screenshotsService;

    public MongoDbScenarioProcessor(ScreenshotsService screenshotsService) {
        this.screenshotsService = screenshotsService;
    }

    @Override
    protected void hasBrowserAnError(UserScenario scenario, WebDriver wd) throws Exception {
        super.hasBrowserAnError(scenario, wd);
    }

    @Override
    protected void processClickExcpetion(int position, Exception ex) throws Exception {
        super.processClickExcpetion(position, ex);
        throw ex;
    }

    @Override
    protected void makeAShot(UserScenario scenario, SeleniumDriver seleniumDriver, WebDriver theWebDriver, int position) {
        MongoDbScenario mongoDbScenario = (MongoDbScenario) scenario;
        DBObject metaData = new BasicDBObject();
        metaData.put("recordingName", mongoDbScenario.getRecordingName());
        metaData.put("recordingId", mongoDbScenario.getRecordingId());
        metaData.put("experimentId", mongoDbScenario.getExperimentId());
        metaData.put("tag", mongoDbScenario.getTag());
        metaData.put("tagHash", mongoDbScenario.getTagHash());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        String dir = scenario.getConfiguration().getCommonConfiguration().getScreenDir()
                + File.separator + Paths.get(scenario.getScenarioFilename()).getFileName().toString();
        String fname = new String(dir.toString() + File.separator + String.format("%05d", position) + ".png");

        try {
            seleniumDriver.makeAShot(theWebDriver, baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            screenshotsService.getGridFsTemplate().store(bais, fname, "image/png", metaData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
