package com.focusit.jsflight.example;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import com.focusit.jsflight.recorder.internalevent.InternalEventRecorder;
import com.focusit.jsflight.recorder.internalevent.httprequest.HttpRecordInformation;
import com.focusit.jsflight.recorder.internalevent.httprequest.HttpRecorderHelper;
import com.focusit.jsflight.recorder.internalevent.httprequest.RecordableHttpServletRequest;

@WebFilter(filterName = "z_filter", asyncSupported = true, urlPatterns = { "/*" })
public class HttpRecorderFilter implements Filter
{
    @Override
    public void destroy()
    {
        InternalEventRecorder.getInstance().stopRecording();
        try
        {
            InternalEventRecorder.getInstance().shutdown();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        HttpRecordInformation info = new HttpRecordInformation();
        RecordableHttpServletRequest requestForRecord = null;
        try
        {
            try
            {
                requestForRecord = HttpRecorderHelper.prepareRequestToRecord((HttpServletRequest)request, info);
            }
            catch (Exception e)
            {
                System.err.println(e.toString());
                e.printStackTrace(System.err);
            }
            chain.doFilter(requestForRecord, response);
        }
        finally
        {
            try
            {
                info.payload = requestForRecord.getPayloadBytes();
                InternalEventRecorder.getInstance().push("HTTPREQUEST", info);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        InternalEventRecorder.getInstance().startRecording();
    }

}
