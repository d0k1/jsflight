package com.focusit.jsflight.server.os;

import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

/**
 * Created by dkolmogortsev on 16.08.16.
 * Takes screenshots from Xvfb using xwd
 */
public class OperatingSystemScreenshooter
{
    /**
     * Takes a screenshot from specified Xvfb display and dumps it in outputstream
     * @param display xvfb display
     * @param output outputstream to store screenshot
     * @throws Exception
     */
    public static void takeXvfbScreenshot(String display, OutputStream output) throws Exception
    {
        if (isWindows())
        {
            throw new UnsupportedOperationException("Taking screenshots by OS on Windows is not supported");
        }
        ProcessBuilder pb = new ProcessBuilder("bash", "-c",
                "xwd -root -display " + normalizeDisplay(display) + " -silent | xwdtopnm | pnmtopng");
        Process process = pb.start();
        //Inputstream of the process will be closed when process is terminated
        IOUtils.copy(process.getInputStream(), output);
        process.waitFor();
    }

    private static boolean isWindows()
    {
        return System.getProperty("os.name").toLowerCase().startsWith("win");
    }

    private static String normalizeDisplay(String display)
    {
        if (display.startsWith(":"))
        {
            return display;
        }
        return ":" + display;
    }
}
