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
        kryo.writeObject(out, original.getParameterMap());
        kryo.writeObject(out, original.getContentLengthLong());
        kryo.writeObject(out, original.getContentType());
        kryo.writeObject(out, original.getRequestURI());

        info.params = stream.toByteArray();
        return new RecordableHttpServletRequest(original);
    }
}
