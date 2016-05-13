package com.focusit.scenario;

import org.json.JSONObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.focusit.jsflight.player.config.Configuration;
import com.focusit.jsflight.player.scenario.UserScenario;
import com.focusit.model.Event;
import com.focusit.model.Experiment;
import com.focusit.repository.EventRepository;

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
        return experiment.getPosition();
    }

    @Override
    public int getStepsCount() {
        return experiment.getSteps();
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

    @Override
    public String getScenarioFilename() {
        return getRecordingName();
    }

    public String getRecordingId(){
        return experiment.getRecordingId();
    }

    public String getRecordingName(){
        return experiment.getRecordingName();
    }

    public String getTag(){
        return experiment.getTag();
    }

    public String getTagHash(){
        return experiment.getTagHash();
    }

    public String getExperimentId(){
        return experiment.getId();
    }

    public int getFirstStep(){
        return experiment.getPosition();
    }

    public int getMaxStep(){
        return experiment.getLimit();
    }
}
