package com.focusit.jsflight.recorder;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

@WebServlet(urlPatterns = { "/jsflight/recorder/download" })
public class RecorderDownloadServlet extends HttpServlet
{

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        StringWriter writer = new StringWriter();
        IOUtils.copy(req.getInputStream(), writer, "UTF-8");
        String theString = writer.toString();

        String result = java.net.URLDecoder.decode(theString, "UTF-8");
        if (result.length() < 5)
        {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        result = result.substring(5, result.length());
        byte[] data = result.getBytes();
        String name = "record_" + System.currentTimeMillis() + ".json";
        resp.setContentType("application/json");
        resp.setHeader("Content-Transfer-Encoding", "binary");
        resp.setHeader("Content-Length", "" + data.length);
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + name + "\"");
        resp.getWriter().println(result);
        resp.getWriter().flush();
    }

}
