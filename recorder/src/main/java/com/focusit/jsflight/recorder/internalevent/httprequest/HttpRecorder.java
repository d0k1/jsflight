package com.focusit.jsflight.recorder.internalevent.httprequest;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.FastOutput;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import com.focusit.jsflight.recorder.internalevent.InternalEventRecorder;

public class HttpRecorder
{
    public static final String HTTP_RECORDER_TAG = "HTTPREQUEST";
    private static ThreadLocal<Kryo> threadKryo = new ThreadLocal<>();

    public static RecordableHttpServletRequest prepareRequestToRecord(HttpServletRequest original,
            HttpRecordInformation info)
    {
        // buffer is 1kb at least
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024 * 1024);

        FastOutput out = new FastOutput(stream);
        Kryo kryo = threadKryo.get();
        if (kryo == null)
        {
            kryo = new Kryo();
            kryo.register(HashMap.class, new MapSerializer());
            kryo.register(ConcurrentHashMap.class, new MapSerializer());
            threadKryo.set(kryo);
        }
        HashMap<String, String[]> item = new HashMap<>();
        if (original.getParameterMap() != null)
        {
            item.putAll(original.getParameterMap());
        }

        HashMap<String, String> headers = new HashMap<>();
        Enumeration<String> names = original.getHeaderNames();
        while (names.hasMoreElements())
        {
            String header = names.nextElement();
            headers.put(header, original.getHeader(header));
        }
        try
        {
            kryo.writeObject(out, item);

            kryo.writeObject(out, headers);

            kryo.writeObjectOrNull(out, original.getContentLength(), Integer.class);

            kryo.writeObjectOrNull(out, original.getContentType(), String.class);

            kryo.writeObjectOrNull(out, original.getRequestURI(), String.class);

            kryo.writeObjectOrNull(out, original.getMethod(), String.class);
            kryo.writeObjectOrNull(out, original.getServletContext().getContextPath(), String.class);

            HashMap<String, String> cookies = new HashMap<>();
            if (original.getCookies() != null && original.getCookies().length > 0)
            {
                for (Cookie cookie : original.getCookies())
                {
                    cookies.put(cookie.getName(), cookie.getValue());
                }
            }

            kryo.writeObject(out, cookies);
            out.flush();
            out.close();

        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }

        info.params = stream.toByteArray();
        return new RecordableHttpServletRequest(original);
    }

    public static void recordRequest(RecordableHttpServletRequest requestForRecord, HttpRecordInformation info)
            throws UnsupportedEncodingException, InterruptedException
    {
        info.payload = requestForRecord.getPayloadBytes();
        InternalEventRecorder.getInstance().push(HTTP_RECORDER_TAG, info);
    }
}
