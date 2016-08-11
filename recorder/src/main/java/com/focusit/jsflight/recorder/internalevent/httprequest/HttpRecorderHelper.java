package com.focusit.jsflight.recorder.internalevent.httprequest;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.FastOutput;
import com.esotericsoftware.kryo.serializers.MapSerializer;

public class HttpRecorderHelper
{
    private static ThreadLocal<Kryo> threadKryo = new ThreadLocal<>();
    private static String NULL_OBJECT = "null_object";

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
        Object item = new HashMap<String, String[]>(original.getParameterMap());
        kryo.writeObject(out, item == null ? NULL_OBJECT : item);

        item = original.getContentLengthLong();
        kryo.writeObject(out, item == null ? NULL_OBJECT : item);

        item = original.getContentType();
        kryo.writeObject(out, item == null ? NULL_OBJECT : item);

        item = original.getRequestURI();
        kryo.writeObject(out, item == null ? NULL_OBJECT : item);

        item = original.getMethod();
        kryo.writeObject(out, item == null ? NULL_OBJECT : item);

        info.params = stream.toByteArray();
        return new RecordableHttpServletRequest(original);
    }
}
