package com.focusit.jsflight.player.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Events
{
    private List<JSONObject> events = new ArrayList<>();

    public List<JSONObject> getEvents()
    {
        return events;
    }

    public void parse(List<String> content)
    {
        JSONArray rawevents;
        for (String line : content)
        {
            rawevents = new JSONArray(line);
            for (int i = 0; i < rawevents.length(); i++)
            {
                String event = rawevents.getString(i);
                if (!event.contains("flight-cp"))
                {
                    events.add(new JSONObject(event));
                }
            }
        }
        Collections.sort(events, new Comparator<JSONObject>()
        {
            @Override
            public int compare(JSONObject o1, JSONObject o2)
            {
                return ((Long)o1.getLong("timestamp")).compareTo(o2.getLong("timestamp"));
            }
        });
    }
}
