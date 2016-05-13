package com.focusit.player;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import com.focusit.jsflight.player.webdriver.SeleniumDriver;
import com.focusit.model.Experiment;
import com.focusit.model.Recording;
import com.focusit.repository.EventRepository;
import com.focusit.repository.ExperimentRepository;
import com.focusit.repository.RecordingRepository;
import com.focusit.scenario.MongoDbScenario;
import com.focusit.scenario.MongoDbScenarioProcessor;
import com.focusit.service.ExperimentFactory;
import com.focusit.service.ScreenshotsService;

/**
 * Component that plays a scenario in background
 *
 * Created by dkirpichenkov on 05.05.16.
 */
@Service
public class BackgroundWebPlayer {
    @Inject
    ScreenshotsService screenshotsService;
    @Inject
    private RecordingRepository recordingRepository;
    @Inject
    private EventRepository eventRepository;
    @Inject
    private ExperimentRepository experimentRepository;
    private Map<String, CompletableFuture> playingFutures = new HashMap<>();

    public Experiment start(String recordingId, boolean withScreenshots, boolean paused, boolean background){
        Recording rec = recordingRepository.findOne(new ObjectId(recordingId));
        if(rec==null){
            throw new IllegalArgumentException("no recording found for id "+recordingId);
        }

        Experiment experiment = new ExperimentFactory().get();
        experiment.setCreated(new Date());
        experiment.setRecordingName(rec.getName());
        experiment.setRecordingId(rec.getId());
        experiment.setScreenshots(withScreenshots);
        experiment.setSteps(eventRepository.countByRecordingId(recordingId).intValue());
        experiment.setPosition(0);
        experiment.setLimit(0);

        experimentRepository.save(experiment);

        if(!Boolean.TRUE.equals(paused)) {
            resume(experiment.getId());
        };

        return experiment;

    }

    public void resume(String experimentId){
        Experiment experiment = experimentRepository.findOne(new ObjectId(experimentId));
        if(experiment==null){
            throw new IllegalArgumentException("No experiment found by given id "+experimentId);
        }

        experiment.setPlaying(true);
        experimentRepository.save(experiment);

        MongoDbScenario scenario = new MongoDbScenario(experiment, eventRepository);
        MongoDbScenarioProcessor processor = new MongoDbScenarioProcessor(screenshotsService);

        playingFutures.put(experimentId, CompletableFuture.runAsync(() -> {
            processor.play(scenario, new SeleniumDriver(scenario), scenario.getFirstStep(), scenario.getMaxStep());
        }).whenCompleteAsync((aVoid, throwable) -> {
            if (throwable instanceof PausePlaybackException) {

            } else if (throwable instanceof TerminatePlaybackException) {

            } else {

            }
        }));
    }

    public void pause(String experimentId){

    }

    public void cancel(String experimentId){

    }

    public Experiment status(String experimentId){
        Experiment experiment = experimentRepository.findOne(new ObjectId(experimentId));

        if(experiment==null){
            throw new IllegalArgumentException("no experiment found for id "+experimentId);
        }

        return experiment;
    }

    public InputStream getScreenshot(String experimentId, int step){
        return null;
    }

    public void move(String experimentId, int step){

    }

    public void terminable(){

    }

    public List<Experiment> getAllExperiments() {
        ArrayList<Experiment> result = new ArrayList<>();
        experimentRepository.findAll().forEach(result::add);
        return result;
    }
}
