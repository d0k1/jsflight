package com.focusit.jsflight.player;

import java.awt.EventQueue;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.focusit.jsflight.player.controller.IUIController;
import com.focusit.jsflight.player.controller.InputController;
import com.focusit.jsflight.player.controller.JMeterController;
import com.focusit.jsflight.player.controller.OptionsController;
import com.focusit.jsflight.player.controller.PostProcessController;
import com.focusit.jsflight.player.controller.ScenarioController;
import com.focusit.jsflight.player.controller.WebLookupController;
import com.focusit.jsflight.player.ui.ExceptionDialog;
import com.focusit.jsflight.player.ui.MainFrame;

public class Player
{
    private static final Logger log = LoggerFactory.getLogger(Player.class);
    public static String firefoxPath = null;
    public static boolean ignoreXhr = true;

    public static void main(String[] args) throws Exception
    {

        firefoxPath = args[0];
        try
        {
            InputController.getInstance().load(IUIController.defaultConfig);
            JMeterController.getInstance().load(IUIController.defaultConfig);
            OptionsController.getInstance().load(IUIController.defaultConfig);
            PostProcessController.getInstance().load(IUIController.defaultConfig);
            ScenarioController.getInstance().load(IUIController.defaultConfig);
            WebLookupController.getInstance().load(IUIController.defaultConfig);
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }

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
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
