package com.focusit.jsflight.recorder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 * Servlet provides a js to track events in a browser
 * 
 * @author Denis V. Kirpichenkov
 *
 */
@WebServlet(urlPatterns = { "/jsflight/recorder", "/jsflight/recorder.min.js.map" })
public class RecorderServlet extends HttpServlet
{

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String filename = "recorder.js";
        if (req.getParameter("min") != null)
        {
            filename = "recorder.min.js";
        }

        if (req.getRequestURI().endsWith("recorder.min.js.map"))
        {
            filename = "recorder.min.js.map";
        }

        String ext = req.getParameter("ext");
        try (InputStream is = ext == null ? getStreamFromResource(filename) : getStreamFromExternalFile(ext))
        {
            resp.setContentType("application/javascript");
            try (OutputStream out = resp.getOutputStream())
            {
                IOUtils.copy(is, out);
                out.flush();
            }
        }
    }

    private InputStream getStreamFromExternalFile(String filename) throws FileNotFoundException
    {
        return new FileInputStream(getServletContext().getRealPath(filename));
    }

    private InputStream getStreamFromResource(String filename)
    {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
    }
}
