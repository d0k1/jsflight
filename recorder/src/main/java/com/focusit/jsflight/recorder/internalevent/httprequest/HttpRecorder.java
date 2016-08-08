package com.focusit.jsflight.recorder.internalevent.httprequest;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;

/**
 * Servlet filter that intersepts all request to store them
 * Created by doki on 30.07.16.
 */
@WebFilter(value = "/**", asyncSupported = true, filterName = "http-recorder")
public class HttpRecorder implements Filter
{
    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {

    }

    @Override
    public void destroy()
    {

    }
}
