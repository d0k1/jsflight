package com.focusit.jsflight.recorder;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Servlet to process tracked data from a browser.
 * It's logic can be overrided by {@link RecordingProcessor}
 *
 * @author Denis V. Kirpichenkov
 */
@WebServlet(urlPatterns = { "/jsflight/recorder/storage" })
public class RecorderStorageServlet extends HttpServlet
{

    private static final long serialVersionUID = 1L;

    private static volatile RecordingProcessor processor = new ExampleRecordingProcessor();

    public static void setProcessor(RecordingProcessor processor)
    {
        RecorderStorageServlet.processor = processor;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        RecordingProcessor recProcess = processor;
        if (recProcess == null)
        {
            resp.getWriter().print("{\"OK\"}");
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        StringWriter writer = new StringWriter();
        IOUtils.copy(req.getInputStream(), writer, "UTF-8");
        String data = writer.toString();
        try
        {
            data = java.net.URLDecoder.decode(data, "UTF-8");
        }
        catch (Exception ex)
        {
            recProcess.processError(req, resp, data);
        }

        if (data.length() < 5)
        {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 'cause form's data starts with 'data='
        data = data.substring(5, data.length());
        if (req.getParameter("download") != null)
        {
            recProcess.processDownloadRequest(req, resp, data);
        }
        else if (req.getParameter("stop") != null)
        {
            recProcess.processRecordStop(req, resp, data);
        }
        else
        {
            recProcess.processStoreEvent(req, resp, data);
        }
    }
}
