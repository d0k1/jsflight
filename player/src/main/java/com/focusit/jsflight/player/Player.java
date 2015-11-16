package com.focusit.jsflight.player;

import java.awt.EventQueue;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.TaskDialogs;
import com.focusit.jsflight.player.ui.MainFrame;

public class Player
{
    private static final Logger log = LoggerFactory.getLogger(Player.class);

    public static String firefoxPath = null;

    public static void main(String[] args) throws IOException
    {

        firefoxPath = args[0];

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler()
        {

            @Override
            public void uncaughtException(Thread t, Throwable e)
            {
                log.error(e.toString(), e);
                TaskDialogs.showException(e);
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

    public static String readFile(String path, Charset encoding) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
