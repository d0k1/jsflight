package com.focusit.service;

import com.focusit.jsflight.player.config.Configuration;
import com.focusit.model.Experiment;

/**
 * Factory to provide an Experiment with default cconfiguration
 * Created by doki on 12.05.16.
 */
public class ExperimentFactory {

    public Experiment get(){
        Experiment result = new Experiment();

        Configuration cfg = new Configuration();
        cfg.getCommonConfiguration().loadDefaultValues();
        cfg.getCommonConfiguration().setUseFirefox(true);
        cfg.getWebConfiguration().loadDefaults();
        cfg.getjMeterConfiguration().loadDefaults();
        result.setConfiguration(cfg);
        return result;
    }
}
