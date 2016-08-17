package com.focusit.jsflight.player.http2jmeter;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.FastInput;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import com.focusit.jsflight.recorder.internalevent.InternalEventRecorder;
import com.focusit.jsflight.recorder.internalevent.httprequest.HttpRecordInformation;

/**
 * Created by dkirpichenkov on 17.08.16.
 */
public class Http2JMeter
{
    private static final String GUEST_USER = "GUEST";

    static class RestoredRequest
    {
        Map<String, String[]> parameters;
        Integer contentLength;
        String contentType;
        String uri;
        String method;
        String contextPath;
        HashMap<String, String> cookies;
        String payload;
        HashMap additional;

        @Override
        public String toString()
        {
            return "RestoredRequest{" + "parameters=" + parameters + ", contentLength=" + contentLength
                    + ", contentType='" + contentType + '\'' + ", uri='" + uri + '\'' + ", method='" + method + '\''
                    + ", contextPath='" + contextPath + '\'' + ", cookies=" + cookies + ", payload=" + payload
                    + ", additional=" + additional + '}';
        }
    }

    public List<RestoredRequest> getRequests(String file) throws IOException
    {
        List<RestoredRequest> requests = new ArrayList<>();
        FastInput input = new FastInput(new FileInputStream(file));

        Kryo kryo = new Kryo();
        kryo.register(HashMap.class, new MapSerializer());
        while (input.available() > 0)
        {
            InternalEventRecorder.InternalEventRecord record = kryo.readObject(input,
                    InternalEventRecorder.InternalEventRecord.class);
            HttpRecordInformation information = (HttpRecordInformation)record.data;

            String tag = new String(record.tag).trim();

            if (tag.equalsIgnoreCase(
                    com.focusit.jsflight.recorder.internalevent.httprequest.HttpRecorder.HTTP_RECORDER_TAG))
            {

                RestoredRequest request = new RestoredRequest();

                if (information.params != null && information.params.length > 0)
                {
                    FastInput paramsInput = new FastInput(new ByteArrayInputStream(information.params));

                    request.parameters = kryo.readObject(paramsInput, HashMap.class);
                    request.contentLength = kryo.readObjectOrNull(paramsInput, Integer.class);
                    request.contentType = kryo.readObjectOrNull(paramsInput, String.class);
                    request.uri = kryo.readObjectOrNull(paramsInput, String.class);
                    request.method = kryo.readObjectOrNull(paramsInput, String.class);
                    request.contextPath = kryo.readObjectOrNull(paramsInput, String.class);
                    request.cookies = kryo.readObject(paramsInput, HashMap.class);
                }
                if (information.payload != null && information.payload.length > 0)
                {
                    request.payload = new String(information.payload);
                }
                else
                {

                }
                request.additional = information.additional;
                requests.add(request);

            }
        }
        return requests;
    }

    public Map<String, List<RestoredRequest>> getRequestsByUsers(List<RestoredRequest> requests)
    {
        HashMap<String, List<RestoredRequest>> result = new HashMap<>();

        for (RestoredRequest request : requests)
        {
            String[] uuids = request.parameters.get("uuid");
            if (uuids == null)
            {
                uuids = new String[] { GUEST_USER };
            }
            if (uuids != null && uuids.length > 0)
            {
                String uuid = uuids[0];
                List<RestoredRequest> filtered = result.get(request.parameters.get("uuid"));
                if (filtered == null)
                {
                    filtered = new ArrayList<>();
                    result.put(uuid, filtered);
                }

                filtered.add(request);
            }
        }

        return result;
    }

    public static void main(String[] args) throws IOException
    {
        Http2JMeter converter = new Http2JMeter();
        List<RestoredRequest> requests = converter.getRequests(args[0]);
        Map<String, List<RestoredRequest>> byUuid = converter.getRequestsByUsers(requests);

        System.out.println(byUuid.size());
    }
}
