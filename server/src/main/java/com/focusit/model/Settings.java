package com.focusit.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for server stored in mongodb
 * Created by dkirpichenkov on 29.04.16.
 */
@Document
public class Settings {
    public static final String SETTINGS_ID = "55b5ffa5511fee0e45ed614b";

    @Id
    private String id = SETTINGS_ID;
    private List<String> alarmEmail = new ArrayList<>();

    public List<String> getAlarmEmail() {
        return alarmEmail;
    }

    public void setAlarmEmail(List<String> alarmEmail) {
        this.alarmEmail = alarmEmail;
    }
}
