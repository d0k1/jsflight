package com.focusit.service;

import org.springframework.stereotype.Service;

import com.focusit.scenario.MongoDbScenario;

/**
 * Email sender.
 * Notifies about any possible problem by email
 * Created by doki on 14.05.16.
 */
@Service
public class EmailNotificationService
{

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
