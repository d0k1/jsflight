package com.focusit.jsflight.recorder.internalevent.httprequest;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.focusit.jsflight.recorder.internalevent.InternalEventRecorder;
import com.focusit.jsflight.recorder.internalevent.InternalEventRecorderBuilder;

public abstract class HttpRecorderFilterBase implements Filter
{
    public static final String ALREADY_FILTERED_SUFFIX = ".FILTERED";
    private final AtomicBoolean ENABLED = new AtomicBoolean(false);

    private InternalEventRecorder internalEventRecorder;

    protected InternalEventRecorder getRecorder()
    {
        return internalEventRecorder;
    }

    public void setEnabled(boolean enabled)
    {
        if (!ENABLED.getAndSet(enabled) && enabled)
        {
            internalEventRecorder.openFileForWriting();
        }
    }

    @Override
    public final void destroy()
    {
        try
        {
            internalEventRecorder.shutdown();
        }
        catch (InterruptedException e)
        {
            logException(e);
        }
    }

    @Override
    public final void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws ServletException, IOException
    {

        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse))
        {
            throw new ServletException("OncePerRequestFilter just supports HTTP requests");
        }
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse)response;

        String alreadyFilteredAttributeName = getAlreadyFilteredAttributeName();
        boolean hasAlreadyFilteredAttribute = request.getAttribute(alreadyFilteredAttributeName) != null;

        if (hasAlreadyFilteredAttribute || skipDispatch() || doNotRecordRequest(httpRequest, httpResponse))
        {

            // Proceed without invoking this filter...
            filterChain.doFilter(request, response);
        }
        else
        {
            // Do invoke this filter...
            request.setAttribute(alreadyFilteredAttributeName, Boolean.TRUE);
            try
            {
                doFilterInternal(httpRequest, httpResponse, filterChain);
            }
            finally
            {
                // Remove the "already filtered" request attribute for this request.
                request.removeAttribute(alreadyFilteredAttributeName);
            }
        }
    }

    public final void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        if (!ENABLED.get())
        {
            chain.doFilter(request, response);
            return;
        }

        if (doNotRecordRequest(request, response))
        {
            chain.doFilter(request, response);
            return;
        }

        HttpRecordInformation info = new HttpRecordInformation();
        RecordableHttpServletRequest requestForRecord = null;
        try
        {
            try
            {
                prepareHttpRecordInfo(info, request, response);
                requestForRecord = HttpRecorderHelper.prepareRequestToRecord(request, info);
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
                info.payload = requestForRecord.getPayloadBytes();
                internalEventRecorder.push(HttpRecorderHelper.HTTP_RECORDER_TAG, info);
            }
            catch (InterruptedException e)
            {
                logException(e);
            }
        }
    }

    @Override
    public final void init(FilterConfig filterConfig) throws ServletException
    {
        internalEventRecorder.startRecording();
    }

    public abstract void prepareHttpRecordInfo(HttpRecordInformation info, ServletRequest request,
            ServletResponse response);

    public abstract void updateHttpRecordInfo(HttpRecordInformation info, ServletRequest request,
            ServletResponse response);

    protected abstract boolean doNotRecordRequest(HttpServletRequest request, HttpServletResponse response);

    protected String getAlreadyFilteredAttributeName()
    {
        String name = "HttpRecorderHelper";
        return name + ALREADY_FILTERED_SUFFIX;
    }

    protected abstract void logException(Exception e);

    protected final void initInternal(String fileName)
    {
        initInternal(fileName, null);
    }

    protected final void initInternal(String fileName, String storageThreadName)
    {
        InternalEventRecorderBuilder builder = InternalEventRecorderBuilder.builderFor(fileName);
        if (storageThreadName != null)
        {
            builder.storageThreadName(storageThreadName);
        }
        internalEventRecorder = builder.build();
        if (ENABLED.get())
        {
            internalEventRecorder.openFileForWriting();
        }
    }

    protected boolean shouldNotFilterAsyncDispatch()
    {
        return false;
    }

    protected boolean shouldNotFilterErrorDispatch()
    {
        return false;
    }

    private boolean skipDispatch()
    {
        return shouldNotFilterErrorDispatch();
    }
}
