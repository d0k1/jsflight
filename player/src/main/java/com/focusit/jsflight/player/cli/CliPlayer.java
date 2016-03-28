package com.focusit.jsflight.player.cli;

import com.focusit.jmeter.JMeterRecorder;
import com.focusit.jmeter.JMeterScriptProcessor;
import com.focusit.jsflight.player.scenario.UserScenario;
import com.focusit.jsflight.player.webdriver.SeleniumDriverConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CliPlayer
{
    private static final Logger LOG = LoggerFactory.getLogger(CliPlayer.class);

    private CliConfig config;

    private JMeterRecorder jmeter = new JMeterRecorder();

    public CliPlayer(CliConfig config) throws Exception
    {
        this.config = config;
        String templatePath = config.getJmxTemplatePath();
        if (templatePath.trim().isEmpty())
        {
            LOG.info("Initializing Jmeter with default jmx template: template.jmx");
            jmeter.init();
        }
        else
        {
            LOG.info("Initializing Jmeter with jmx template: {}", templatePath);
            jmeter.init(templatePath);
        }

        updateControllers();
    }

    private void updateControllers() throws IOException {
        if(this.config.getJmeterStepPreprocess()!=null && !this.config.getJmeterStepPreprocess().trim().isEmpty()) {
            JMeterScriptProcessor.getInstance().setRecordingScript(new String(Files.readAllBytes(Paths.get(config.getJmeterStepPreprocess().trim())), "UTF-8"));
        }

        if(this.config.getJmeterScenarioPreprocess()!=null && !this.config.getJmeterScenarioPreprocess().trim().isEmpty()){
            JMeterScriptProcessor.getInstance().setProcessScript(new String(Files.readAllBytes(Paths.get(config.getJmeterScenarioPreprocess().trim())), "UTF-8"));
        }
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
                scenario.play(Integer.parseInt(config.getStartStep()), Integer.parseInt(config.getFinishStep()));
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
