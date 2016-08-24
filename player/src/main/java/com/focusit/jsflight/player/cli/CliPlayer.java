package com.focusit.jsflight.player.cli;

import com.focusit.jmeter.JMeterRecorder;
import com.focusit.jsflight.player.scenario.ScenarioProcessor;
import com.focusit.jsflight.player.scenario.UserScenario;
import com.focusit.jsflight.player.webdriver.SeleniumDriver;
import com.focusit.jsflight.utils.StringUtils;
import com.focusit.script.ScriptEngine;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CliPlayer
{
    private static final Logger LOG = LoggerFactory.getLogger(CliPlayer.class);

    private CliConfig config;

    private JMeterRecorder jmeter;

    private SeleniumDriver seleniumDriver;

    public CliPlayer(CliConfig config) throws Exception
    {
        this.config = config;
    }

    private void prepareJmeterIfNeeded(UserScenario scenario) throws Exception
    {
        ScriptEngine.init(scenario.getConfiguration().getCommonConfiguration().getScriptClassloader());
        jmeter = new JMeterRecorder();
        String templatePath = config.getJmxTemplatePath();
        if (StringUtils.isEmptyOrWhiteSpace(templatePath))
        {
            LOG.info("Initializing Jmeter with default jmx template: template.jmx");
            jmeter.init();
        }
        else
        {
            LOG.info("Initializing Jmeter with jmx template: {}", templatePath);
            jmeter.init(templatePath);
        }

    }

    public SeleniumDriver getSeleniumDriver()
    {
        return seleniumDriver;
    }

    public void play() throws Exception
    {
        UserScenario scenario = new UserScenario();

        updateControllers(scenario);
        prepareJmeterIfNeeded(scenario);

        LOG.info("Loading {}", config.getPathToRecording());
        scenario.parse(config.getPathToRecording());
        scenario.postProcessScenario();
        seleniumDriver = new SeleniumDriver(scenario);
        scenario.getContext().setJMeterBridge(jmeter.getBridge());
        if (config.isEnableRecording())
        {
            jmeter.setProxyPort(Integer.parseInt(config.getProxyPort()));
            jmeter.startRecording();
            try
            {
                new ScenarioProcessor().play(scenario, seleniumDriver, Integer.parseInt(config.getStartStep()),
                        Integer.parseInt(config.getFinishStep()));
            }
            finally
            {
                jmeter.stopRecording();
                LOG.info("Saving recorded scenario to {}", config.getJmeterRecordingName());
                jmeter.saveScenario(config.getJmeterRecordingName());
            }
        }
        else
        {
            new ScenarioProcessor().play(scenario, seleniumDriver, Integer.parseInt(config.getStartStep()),
                    Integer.parseInt(config.getFinishStep()));
        }
    }

    private void updateControllers(UserScenario scenario) throws IOException
    {
        if (!StringUtils.isNullOrEmptyOrWhiteSpace(this.config.getJmeterStepPreprocess()))
        {
            jmeter.getScriptProcessor().setRecordingScript(
                    new String(Files.readAllBytes(Paths.get(config.getJmeterStepPreprocess().trim())), "UTF-8"));
        }

        if (!StringUtils.isNullOrEmptyOrWhiteSpace(this.config.getJmeterScenarioPreprocess()))
        {
            jmeter.getScriptProcessor().setProcessScript(
                    new String(Files.readAllBytes(Paths.get(config.getJmeterScenarioPreprocess().trim())), "UTF-8"));
        }

        //Init options, based on config

        scenario.getConfiguration().getCommonConfiguration().setFfPath(config.getFfPath());
        scenario.getConfiguration().getCommonConfiguration().setMakeShots(config.getMakeShots());
        scenario.getConfiguration().getCommonConfiguration().setPageReadyTimeout(config.getPageReadyTimeout());
        scenario.getConfiguration().getCommonConfiguration().setProxyHost(config.getProxyHost());
        scenario.getConfiguration().getCommonConfiguration().setProxyPort(config.getProxyPort());
        scenario.getConfiguration().getCommonConfiguration().setScreenDir(config.getScreenDir());
        scenario.getConfiguration().getCommonConfiguration().setUseFirefox(config.isUseFirefox());
        scenario.getConfiguration().getCommonConfiguration().setUsePhantomJs(config.isUsePhantomJs());
        scenario.getConfiguration().getCommonConfiguration().setPjsPath(config.getPjsPath());
        scenario.getConfiguration().getCommonConfiguration().setUseRandomChars(config.isUseRandomChars());
        scenario.getConfiguration().getCommonConfiguration().setFormOrDialogXpath(config.getFormDialogXpath());

        scenario.getConfiguration().getCommonConfiguration().loadDefaultValues();

        //Init weblookup script
        scenario.getConfiguration().getWebConfiguration()
                .setLookupScript(FileUtils.readFileToString(new File(config.getWebLookupScriptPath())));

        //Init duplicate handler script
        scenario.getConfiguration().getWebConfiguration()
                .setDuplicationScript(FileUtils.readFileToString(new File(config.getDuplicateHandlerScriptPath())));

        //Init script events handler
        scenario.getConfiguration().getScriptEventConfiguration()
                .setScript(FileUtils.readFileToString(new File(config.getScriptEventHandlerScriptPath())));
    }
}
