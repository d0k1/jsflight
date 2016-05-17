package com.focusit.player;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.focusit.jmeter.JMeterRecorder;
import com.focusit.jsflight.player.webdriver.SeleniumDriver;
import com.focusit.model.Experiment;
import com.focusit.model.Recording;
import com.focusit.repository.EventRepository;
import com.focusit.repository.ExperimentRepository;
import com.focusit.repository.RecordingRepository;
import com.focusit.scenario.MongoDbScenario;
import com.focusit.scenario.MongoDbScenarioProcessor;
import com.focusit.service.EmailNotificationService;
import com.focusit.service.ExperimentFactory;
import com.focusit.service.MongoDbStorageService;

/**
 * Component that plays a scenario in background
 *
 * Created by dkirpichenkov on 05.05.16.
 */
@Service
public class BackgroundWebPlayer
{
    private static final Logger LOG = LoggerFactory.getLogger(BackgroundWebPlayer.class);
    private MongoDbStorageService screenshotsService;
    private RecordingRepository recordingRepository;
    private EventRepository eventRepository;
    private ExperimentRepository experimentRepository;
    private EmailNotificationService notificationService;

    private Map<String, CompletableFuture> playingFutures = new ConcurrentHashMap<>();
    private Map<String, JMeterRecorder> jmeters = new ConcurrentHashMap<>();

    private List<Integer> availablePorts = new ArrayList<>(64356);
    private ReentrantLock jmeterStartStopLock = new ReentrantLock();

    @Inject
    public BackgroundWebPlayer(MongoDbStorageService screenshotsService, RecordingRepository recordingRepository,
            EventRepository eventRepository, ExperimentRepository experimentRepository,
            EmailNotificationService notificationService)
    {
        this.screenshotsService = screenshotsService;
        this.recordingRepository = recordingRepository;
        this.eventRepository = eventRepository;
        this.experimentRepository = experimentRepository;
        this.notificationService = notificationService;

        for (int i = 1025; i < 64530; i++)
        {
            availablePorts.add(i);
        }
    }

    public Experiment start(String recordingId, boolean withScreenshots, boolean paused) throws Exception
    {
        Recording rec = recordingRepository.findOne(new ObjectId(recordingId));
        if (rec == null)
        {
            throw new IllegalArgumentException("no recording found for id " + recordingId);
        }

        Experiment experiment = new ExperimentFactory().get();
        experiment.setCreated(new Date());
        experiment.setRecordingName(rec.getName());
        experiment.setRecordingId(rec.getId());
        experiment.setScreenshots(withScreenshots);
        experiment.setSteps((int)eventRepository.countByRecordingId(new ObjectId(recordingId)));
        experiment.setPosition(0);
        experiment.setLimit(0);

        experimentRepository.save(experiment);

        if (!Boolean.TRUE.equals(paused))
        {
            resume(experiment.getId());
        }

        return experiment;

    }

    private void startJMeter(MongoDbScenario scenario) throws Exception
    {
        if (!scenario.getConfiguration().getCommonConfiguration().getProxyPort().isEmpty())
        {
            return;
        }
        if (jmeterStartStopLock.tryLock() || jmeterStartStopLock.tryLock(10, TimeUnit.SECONDS))
        {
            if (availablePorts.size() < 0)
            {
                throw new IllegalArgumentException("No ports left to start JMeter");
            }
        }
        else
        {
            LOG.error("Can't acquire a lock to start JMeter");
        }
        try
        {
            int port = availablePorts.get(0);
            availablePorts.remove(0);
            scenario.getConfiguration().getCommonConfiguration().setProxyPort("" + port);
            JMeterRecorder recorder = new JMeterRecorder(
                    scenario.getConfiguration().getCommonConfiguration().getScriptClassloader());
            recorder.init();
            recorder.setProxyPort(port);
            jmeters.put(scenario.getExperimentId(), recorder);
            recorder.startRecording();
        }
        catch (IOException e)
        {
            LOG.error(e.toString(), e);
            throw e;
        }
        finally
        {
            jmeterStartStopLock.unlock();
        }
    }

    private void stopJMeter(MongoDbScenario scenario) throws Exception
    {
        if (scenario.getConfiguration().getCommonConfiguration().getProxyPort().isEmpty())
        {
            return;
        }

        if (scenario.getConfiguration().getCommonConfiguration().getProxyPort().equalsIgnoreCase("-1"))
        {
            return;
        }

        if (!jmeterStartStopLock.tryLock() && !jmeterStartStopLock.tryLock(10, TimeUnit.SECONDS))
        {
            LOG.error("Can't acquire a lock to stop JMeter");
        }
        try
        {
            JMeterRecorder recorder = jmeters.get(scenario.getExperimentId());
            if (recorder == null)
            {
                throw new IllegalStateException("Instance of JMeterRecorder not found");
            }

            recorder.stopRecording();

            availablePorts.add(Integer.parseInt(scenario.getConfiguration().getCommonConfiguration().getProxyPort()));
            scenario.getConfiguration().getCommonConfiguration().setProxyPort("");

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
            {
                recorder.saveScenario(baos);
                try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray()))
                {
                    screenshotsService.storeJMeterScenario(scenario, bais);
                }
            }
        }
        finally
        {
            jmeterStartStopLock.unlock();
        }
    }

    public void resume(String experimentId) throws Exception
    {
        Experiment experiment = experimentRepository.findOne(new ObjectId(experimentId));
        if (experiment == null)
        {
            throw new IllegalArgumentException("No experiment found by given id " + experimentId);
        }

        experiment.setPlaying(true);
        int stepCount = (int)eventRepository.countByRecordingId(new ObjectId(experiment.getRecordingId()));
        experiment.setSteps(stepCount);
        experimentRepository.save(experiment);

        MongoDbScenario scenario = new MongoDbScenario(experiment, eventRepository, experimentRepository);
        MongoDbScenarioProcessor processor = new MongoDbScenarioProcessor(screenshotsService);

        startJMeter(scenario);
        experimentRepository.save(experiment);

        playingFutures.put(experimentId, CompletableFuture.runAsync(() -> {
            processor.play(scenario, new SeleniumDriver(scenario), scenario.getFirstStep(), scenario.getMaxStep());
        }).whenCompleteAsync((aVoid, throwable) -> {
            playingFutures.remove(experimentId);

            if (throwable == null)
            {
                experiment.setPlaying(false);
                experiment.setFinished(true);
                experimentRepository.save(experiment);
                notificationService.notifyScenarioDone(scenario, throwable);
            }
            else
            {
                LOG.error(throwable.toString(), throwable);
                if (throwable instanceof PausePlaybackException)
                {
                    experiment.setPlaying(false);
                    experimentRepository.save(experiment);
                    notificationService.notifyScenarioPaused(scenario, null);
                    return;
                }
                else if (throwable instanceof ErrorInBrowserPlaybackException)
                {
                    experiment.setPlaying(false);
                    experimentRepository.save(experiment);
                    notificationService.notifyErrorInBrowserOccured(scenario, throwable);
                    return;
                }
                else if (throwable instanceof TerminatePlaybackException)
                {
                    experiment.setPlaying(false);
                    experiment.setFinished(true);
                    experimentRepository.save(experiment);
                    notificationService.notifyScenarioTerminated(scenario, throwable);
                }
                else
                {
                    experiment.setPlaying(false);
                    experiment.setFinished(false);
                    experiment.setError(true);
                    experiment.setErrorMessage(throwable.toString());
                    experimentRepository.save(experiment);
                    notificationService.notifyUnknownException(scenario, throwable);
                    return;
                }
            }
            try
            {
                stopJMeter(scenario);
                experimentRepository.save(experiment);
            }
            catch (Exception e)
            {
                LOG.error(e.toString(), e);
            }
        }));
    }

    public void pause(String experimentId)
    {
        CompletableFuture future = playingFutures.get(experimentId);
        if (future == null)
        {
            throw new IllegalArgumentException("Experiment " + experimentId + " is not playing now");
        }
        future.completeExceptionally(new PausePlaybackException());
    }

    public void cancel(String experimentId)
    {
        CompletableFuture future = playingFutures.get(experimentId);
        if (future == null)
        {
            throw new IllegalArgumentException("Experiment " + experimentId + " is not playing now");
        }
        future.completeExceptionally(new TerminatePlaybackException());
    }

    public Experiment status(String experimentId)
    {
        Experiment experiment = experimentRepository.findOne(new ObjectId(experimentId));

        if (experiment == null)
        {
            throw new IllegalArgumentException("no experiment found for id " + experimentId);
        }

        return experiment;
    }

    public InputStream getScreenshot(String experimentId, int step)
    {
        Experiment experiment = experimentRepository.findOne(new ObjectId(experimentId));
        return screenshotsService.getScreenshot(experiment.getRecordingName(), experimentId, step);
    }

    public void move(String experimentId, int step)
    {
        Experiment experiment = experimentRepository.findOne(new ObjectId(experimentId));
        experiment.setPosition(step);
        experimentRepository.save(experiment);
    }

    public void terminable()
    {

    }

    public List<Experiment> getAllExperiments()
    {
        ArrayList<Experiment> result = new ArrayList<>();
        experimentRepository.findAll().forEach(result::add);
        return result;
    }

    public InputStream getJMX(String experimentId)
    {
        Experiment experiment = experimentRepository.findOne(new ObjectId(experimentId));
        return screenshotsService.getJMeterScenario(experiment.getRecordingName(), experimentId);
    }
}
