package com.focusit.jsflight.recorder.internalevent.httprequest;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import com.focusit.jsflight.recorder.internalevent.InternalEventRecorder;

public abstract class HttpRecorderFilterBase implements Filter
{
    private static final AtomicBoolean enabled = new AtomicBoolean(false);

    public final static void setEnabled(boolean enabled)
    {
        HttpRecorderFilterBase.enabled.set(enabled);
    }

    protected abstract void logException(Exception e);

    protected abstract boolean doNotRecordRequest(ServletRequest request, ServletResponse response);

    @Override
    public final void init(FilterConfig filterConfig) throws ServletException
    {
        InternalEventRecorder.getInstance().startRecording();
    }

    public abstract void prepareHttpRecordInfo(HttpRecordInformation info, ServletRequest request,
            ServletResponse response);

    public abstract void updateHttpRecordInfo(HttpRecordInformation info, ServletRequest request,
            ServletResponse response);

    @Override
    public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        if (!HttpRecorderFilterBase.enabled.get())
        {
            chain.doFilter(request, response);
        }

        if (doNotRecordRequest(request, response))
        {
            chain.doFilter(request, response);
        }

        HttpRecordInformation info = new HttpRecordInformation();
        RecordableHttpServletRequest requestForRecord = null;
        try
        {
            try
            {
                prepareHttpRecordInfo(info, request, response);
                requestForRecord = HttpRecorder.prepareRequestToRecord((HttpServletRequest)request, info);
            }
            catch (Exception e)
            {
                logException(e);
            }
            chain.doFilter(requestForRecord, response);
        }
        finally
        {
            try
            {
                updateHttpRecordInfo(info, requestForRecord, response);
                HttpRecorder.recordRequest(requestForRecord, info);
            }
            catch (InterruptedException e)
            {
                logException(e);
            }
        }
    }

    @Override
    public final void destroy()
    {
        InternalEventRecorder.getInstance().stopRecording();
        try
        {
            InternalEventRecorder.getInstance().shutdown();
        }
        catch (InterruptedException e)
        {
            logException(e);
        }
    }

}
