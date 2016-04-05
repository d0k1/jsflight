package com.focusit.jsflight.player;

import com.beust.jcommander.JCommander;
import com.focusit.jsflight.player.cli.CliConfig;
import com.focusit.jsflight.player.cli.CliPlayer;
import com.focusit.jsflight.player.controller.*;
import com.focusit.jsflight.player.ui.ExceptionDialog;
import com.focusit.jsflight.player.ui.MainFrame;
import com.focusit.jsflight.player.webdriver.SeleniumDriver;
import org.apache.jorphan.logging.LoggingManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.Thread.UncaughtExceptionHandler;

public class Player
{
    private static final Logger log = LoggerFactory.getLogger(Player.class);

    public static void main(String[] args) throws Exception
    {
        try
        {
            InputController.getInstance().load(IUIController.defaultConfig);
            JMeterController.getInstance().load(IUIController.defaultConfig);
            OptionsController.getInstance().load(IUIController.defaultConfig);
            PostProcessController.getInstance().load(IUIController.defaultConfig);
            ScenarioController.getInstance().load(IUIController.defaultConfig);
            WebLookupController.getInstance().load(IUIController.defaultConfig);
            DuplicateHandlerController.getInstance().load(IUIController.defaultConfig);
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
        CliConfig config = new CliConfig();
        JCommander jc = new JCommander(config, args);
        jc.setProgramName(Player.class.getSimpleName());
        if (config.showHelp())
        {
            jc.usage();
            System.exit(0);
        }

        LoggingManager.setPriority(config.getJMeterLogLevel(), "jmeter");
        LoggingManager.setPriority(config.getJMeterLogLevel(), "jorphan");

        if (config.isHeadLess())
        {
            try
            {
                new CliPlayer(config).play();
            }
            catch (Exception e)
            {
                log.error(e.toString(), e);
                System.exit(1);
            }
            finally
            {
                SeleniumDriver.closeWebDrivers();
                System.exit(0);
            }
        }
        else
        {
            startWithUI();
        }
    }

    private static void startWithUI()
    {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler()
        {

            @Override
            public void uncaughtException(Thread t, Throwable e)
            {
                log.error(e.toString(), e);
                ExceptionDialog ld = new ExceptionDialog("Error", e.toString(), e);
                ld.setVisible(true);
            }
        });
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    MainFrame window = new MainFrame();
                    window.getFrame().setVisible(true);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
}
