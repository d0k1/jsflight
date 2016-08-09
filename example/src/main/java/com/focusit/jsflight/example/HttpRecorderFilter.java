package com.focusit.jsflight.example;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

import com.focusit.jsflight.recorder.internalevent.InternalEventRecorder;
import com.focusit.jsflight.recorder.internalevent.httprequest.HttpRecorderHelper;

@WebFilter(filterName = "z_filter", asyncSupported = true, urlPatterns = { "/*" })
public class HttpRecorderFilter implements Filter
{
    @Override
    public void destroy()
    {
        InternalEventRecorder.getInstance().stopRecording();
        InternalEventRecorder.getInstance().shutdown();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        try
        {
            byte bytes[] = HttpRecorderHelper.serializeRequest(request, new HashMap<String, String>());
            InternalEventRecorder.getInstance().push("HTTP_REQUEST", bytes);
        }
        catch (Exception e)
        {
            System.err.println(e.toString());
            e.printStackTrace(System.err);
        }

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        InternalEventRecorder.getInstance().startRecording();
    }

}
