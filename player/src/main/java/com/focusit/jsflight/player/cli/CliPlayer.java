package com.focusit.jsflight.player.cli;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.focusit.jmeter.JMeterRecorder;
import com.focusit.jsflight.player.scenario.UserScenario;
import com.focusit.jsflight.player.webdriver.SeleniumDriverConfig;

public class CliPlayer
{
    private static final Logger LOG = LoggerFactory.getLogger(CliPlayer.class);

    private CliConfig config;

    private JMeterRecorder jmeter = new JMeterRecorder();

    public CliPlayer(CliConfig config) throws Exception
    {
        this.config = config;
        jmeter.init();
    }

    public void play() throws IOException
    {
        UserScenario scenario = new UserScenario();
        LOG.info("Loading {}", config.getPathToRecording());
        scenario.parse(config.getPathToRecording());
        scenario.postProcessScenario();

        SeleniumDriverConfig.get().updateByCliConfig(config);

        if (config.isEnableRecording())
        {
            jmeter.startRecording();
            try
            {
                scenario.play();
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
            scenario.play();
        }
    }
}
