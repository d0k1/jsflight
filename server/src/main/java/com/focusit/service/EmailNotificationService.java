package com.focusit.service;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import com.focusit.model.Settings;
import com.focusit.scenario.MongoDbScenario;

/**
 * Email sender.
 * Notifies about any possible problem by email
 * Created by doki on 14.05.16.
 */
@Service
public class EmailNotificationService
{
    private static final Logger LOG = LoggerFactory.getLogger(EmailNotificationService.class);
    private SettingsService settingsService;
    private JavaMailSender sender;

    @Inject
    public EmailNotificationService(SettingsService settingsService)
    {
        this.settingsService = settingsService;
    }

    @PostConstruct
    public void init()
    {
        Settings settings = settingsService.getSettings();
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

        if (!settings.getSmtpServer().isEmpty())
        {
            javaMailSender.setHost(settings.getSmtpServer());
        }
        if (!settings.getSmtpPort().isEmpty())
        {
            javaMailSender.setPort(Integer.parseInt(settings.getSmtpPort()));
        }
        javaMailSender.setUsername(settings.getStmpUser());
        javaMailSender.setPassword(settings.getStmpPassword());

        javaMailSender.setJavaMailProperties(getMailProperties(settings));

        sender = javaMailSender;
    }

    private Properties getMailProperties(Settings settings)
    {
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.auth", settings.getSmtpAuth());
        properties.setProperty("mail.smtp.starttls.enable", settings.getSmtpStarttls());
        properties.setProperty("mail.debug", settings.getSmtpMailDebug());
        return properties;
    }

    public void notifyScenarioPaused(MongoDbScenario scenario)
    {

    }

    public void notifyScenarioTerminated(MongoDbScenario scenario)
    {

    }

    public void notifyScenarioDone(MongoDbScenario scenario)
    {

    }

    public void notifyErrorInBrowserOccured(MongoDbScenario scenario)
    {

    }

    public void notifyUnknownException(MongoDbScenario scenario)
    {

    }
}
