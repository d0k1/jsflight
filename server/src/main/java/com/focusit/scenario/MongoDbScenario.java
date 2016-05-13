package com.focusit.scenario;

import com.focusit.jsflight.player.config.Configuration;
import com.focusit.jsflight.player.scenario.UserScenario;
import com.focusit.model.Event;
import com.focusit.model.Experiment;
import com.focusit.repository.EventRepository;
import org.json.JSONObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Created by doki on 12.05.16.
 */
public class MongoDbScenario extends UserScenario {
    private final Experiment experiment;
    private EventRepository repository;

    public MongoDbScenario(Experiment experiment, EventRepository repository) {
        this.experiment = experiment;
        this.repository = repository;
    }

    @Override
    public Configuration getConfiguration() {
        return experiment.getConfiguration();
    }

    @Override
    public int getPosition() {
        return experiment.getPosition().intValue();
    }

    @Override
    public int getStepsCount() {
        return experiment.getSteps().intValue();
    }

    @Override
    public JSONObject getStepAt(int position) {
        Event event = repository.findOneByRecordingId(experiment.getRecordingId(), new PageRequest(position, 1, new Sort(Sort.Direction.DESC, "eventId"))).getContent().get(0);
        JSONObject object = new JSONObject(event);
        return object;
    }

    @Override
    public void next() {
        super.next();
    }
}
