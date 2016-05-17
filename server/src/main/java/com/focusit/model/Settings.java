package com.focusit.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Configuration for server stored in mongodb
 * Created by dkirpichenkov on 29.04.16.
 */
@Document
public class Settings
{
    public static final String SETTINGS_ID = "55b5ffa5511fee0e45ed614b";

    @Id
    private String id = SETTINGS_ID;
    private List<String> alarmEmail = new ArrayList<>();
    private String smtpServer;
    private String smtpPort;
    private String stmpUser;
    private String stmpPassword;
    private String smtpMailDebug;
    private String smtpAuth;
    private String smtpStarttls;

    public List<String> getAlarmEmail()
    {
        return alarmEmail;
    }

    public void setAlarmEmail(List<String> alarmEmail)
    {
        this.alarmEmail = alarmEmail;
    }

    public String getSmtpServer()
    {
        return smtpServer;
    }

    public void setSmtpServer(String smtpServer)
    {
        this.smtpServer = smtpServer;
    }

    public String getSmtpPort()
    {
        return smtpPort;
    }

    public void setSmtpPort(String smtpPort)
    {
        this.smtpPort = smtpPort;
    }

    public String getStmpUser()
    {
        return stmpUser;
    }

    public void setStmpUser(String stmpUser)
    {
        this.stmpUser = stmpUser;
    }

    public String getStmpPassword()
    {
        return stmpPassword;
    }

    public void setStmpPassword(String stmpPassword)
    {
        this.stmpPassword = stmpPassword;
    }

    public String getSmtpAuth()
    {
        return smtpAuth;
    }

    public void setSmtpAuth(String smtpAuth)
    {
        this.smtpAuth = smtpAuth;
    }

    public String getSmtpStarttls()
    {
        return smtpStarttls;
    }

    public void setSmtpStarttls(String smtpStarttls)
    {
        this.smtpStarttls = smtpStarttls;
    }

    public String getSmtpMailDebug()
    {
        return smtpMailDebug;
    }

    public void setSmtpMailDebug(String smtpMailDebug)
    {
        this.smtpMailDebug = smtpMailDebug;
    }
}
