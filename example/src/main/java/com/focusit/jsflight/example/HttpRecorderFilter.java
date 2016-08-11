package com.focusit.jsflight.example;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

import com.focusit.jsflight.recorder.internalevent.httprequest.HttpRecordInformation;
import com.focusit.jsflight.recorder.internalevent.httprequest.HttpRecorderFilterBase;

@WebFilter(filterName = "z_filter", asyncSupported = true, urlPatterns = { "/*" })
public class HttpRecorderFilter extends HttpRecorderFilterBase
{

    @Override
    protected void logException(Exception e)
    {
        e.printStackTrace(System.err);
    }

    @Override
    public void prepareHttpRecordInfo(HttpRecordInformation info, ServletRequest request, ServletResponse response)
    {
    }

    @Override
    public void updateHttpRecordInfo(HttpRecordInformation info, ServletRequest request, ServletResponse response)
    {
    }
}
