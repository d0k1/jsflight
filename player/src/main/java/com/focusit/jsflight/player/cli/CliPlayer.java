package com.focusit.jsflight.player.cli;

import com.focusit.jsflight.jmeter.JMeterRecorder;
import com.focusit.jsflight.player.cli.config.IConfig;
import com.focusit.jsflight.player.configurations.Configuration;
import com.focusit.jsflight.player.scenario.ScenarioProcessor;
import com.focusit.jsflight.player.scenario.UserScenario;
import com.focusit.jsflight.player.webdriver.SeleniumDriver;
import com.focusit.jsflight.script.ScriptEngine;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CliPlayer
{
    private static final Logger LOG = LoggerFactory.getLogger(CliPlayer.class);

    private JMeterRecorder createJmeterInstance(Configuration configuration, String templatePath)
            throws Exception
    {
        JMeterRecorder jmeter;
        if (StringUtils.isBlank(templatePath))
        {
            LOG.info("Initializing Jmeter with default jmx template: {}", JMeterRecorder.DEFAULT_TEMPLATE_PATH);
            jmeter = new JMeterRecorder();
        }
        else
        {
            LOG.info("Initializing Jmeter with jmx template: {}", templatePath);
            jmeter = new JMeterRecorder(templatePath);
        }
        jmeter.initialize(configuration.getCommonConfiguration().getMaxRequestsPerScenario());

        configuration.getScriptsConfiguration().syncScripts(jmeter);

        return jmeter;
    }

    public void play(IConfig config) throws Exception
    {
        UserScenario scenario = new UserScenario();
        scenario.initFromConfig(config);

        ScriptEngine.init(scenario.getConfiguration().getCommonConfiguration().getScriptClassloader());

        LOG.info("Loading recording from {}", config.getPathToRecordingFile());
        scenario.parse(config.getPathToRecordingFile());
        scenario.preProcessScenario();
        SeleniumDriver seleniumDriver = new SeleniumDriver(scenario, config.getXvfbDisplayLowerBound(),
                config.getXvfbDisplayUpperBound());
        try
        {
            if (config.shouldEnableRecording())
            {
                JMeterRecorder jmeter = createJmeterInstance(scenario.getConfiguration(),
                        config.getPathToJmxTemplateFile());
                jmeter.setProxyPort(config.getProxyPort());
                jmeter.startRecording();
                try
                {
                    new ScenarioProcessor().play(scenario, seleniumDriver, config.getStartStep(),
                            config.getFinishStep());
                }
                finally
                {
                    jmeter.stopRecording();
                    LOG.info("Saving recorded scenario to {}", config.getGeneratedJmeterScenarioName());
                    jmeter.saveScenarios(config.getGeneratedJmeterScenarioName());
                }
            }
            else
            {
                new ScenarioProcessor().play(scenario, seleniumDriver, config.getStartStep(), config.getFinishStep());
            }
        }
        finally
        {
            seleniumDriver.closeWebDrivers();
        }
    }
}
