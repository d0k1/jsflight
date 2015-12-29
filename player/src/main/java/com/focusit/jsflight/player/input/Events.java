package com.focusit.jsflight.player.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class Events
{
    private static final String EVENT_ID = "eventId";
    private static final String TIMESTAMP = "timestamp";
    private List<JSONObject> events = new ArrayList<>();

    public List<JSONObject> getEvents()
    {
        return events;
    }

    public void parse(List<String> content)
    {
        JSONArray rawevents;
        Set<JSONObject> temp = new HashSet<>();
        for (String line : content)
        {
            rawevents = new JSONArray(line);
            for (int i = 0; i < rawevents.length(); i++)
            {
                String event = rawevents.get(i).toString();
                if (!event.contains("flight-cp"))
                {
                    temp.add(new JSONObject(event));
                }
            }
        }
        events.addAll(temp);
        Collections.sort(events, new Comparator<JSONObject>()
        {
            @Override
            public int compare(JSONObject o1, JSONObject o2)
            {
                return ((Long)o1.getLong(EVENT_ID)).compareTo(o2.getLong(EVENT_ID));
            }
        });
    }
}
