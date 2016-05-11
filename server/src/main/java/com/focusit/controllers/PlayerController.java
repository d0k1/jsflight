package com.focusit.controllers;

import com.focusit.model.Event;
import com.focusit.model.Experiment;
import com.focusit.model.ExperimentStatus;
import com.focusit.model.Recording;
import com.focusit.repository.EventRepository;
import com.focusit.repository.ExperimentRepository;
import com.focusit.repository.RecordingRepository;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by dkirpichenkov on 29.04.16.
 */
@RestController
@RequestMapping(value="/player")
public class PlayerController {

    private final static Logger LOG = LoggerFactory.getLogger(PlayerController.class);

    @Inject
    private RecordingRepository recordingRepository;
    @Inject
    private EventRepository eventRepository;
    @Inject
    private ExperimentRepository experimnetRepository;

    /**
     * Get list of uploaded scenarios
     *
     *
     * @return
     */
    @RequestMapping(value = "/list", method= RequestMethod.GET)
    public List<Recording> getRecordings(){

        ArrayList result = new ArrayList();
        recordingRepository.findAll().forEach(result::add);
        return result;
    }

    /**
     * Uploading scenario.
     * Parses uploaded file line by line and inserting events to mongodb
     *
     * $ curl -F "name=test.json" -F "file=@./test.json" 127.0.0.1:8080/player/upload
     * @param name
     * @param file
     * @param request
     * @param response
     */
    @RequestMapping(method = RequestMethod.POST, value = "/upload")
    public void uploadScenario(@RequestParam("name") String name,
                               @RequestParam("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        String array = "";

        Recording rec = recordingRepository.findByName(name);
        if(rec==null){
            rec = new Recording();
            rec.setName(name);
            rec = recordingRepository.save(rec);
        }
        Recording finalRec = rec;
        Gson gson = new Gson();

        List<CompletableFuture> operations = new ArrayList<>();

        try(InputStreamReader reader = new InputStreamReader(file.getInputStream(), "UTF-8")){
            try(BufferedReader buffered = IOUtils.toBufferedReader(reader)){
                array = buffered.readLine();

                while(array!=null) {

                    String finalArray = array;

                    operations.add(CompletableFuture.supplyAsync(()->{
                        List<Event> lineEvents = null;
                        JSONArray events = new JSONArray(finalArray);
                        lineEvents = new ArrayList<>(events.length());

                        for (int i = 0; i < events.length(); i++) {
                            JSONObject event = new JSONObject(events.get(i).toString());
                            try {
                                Event e = gson.fromJson(event.toString(), Event.class);
                                if (e == null) {
                                    LOG.error("e==null.\n", event.toString(4));
                                    continue;
                                }
                                lineEvents.add(e);
                            } catch (Throwable t) {
                                LOG.error(t.toString(), t);
                                throw t;
                            }
                        }

                        lineEvents.forEach(event->{
                            event.setRecordingId(finalRec.getId());
                            event.setRecordName(finalRec.getName());
                        });

                        eventRepository.save(lineEvents);

                        return true;
                    }).whenCompleteAsync((aBoolean, throwable) -> {
                        if(throwable!=null){
                            LOG.error(throwable.toString(), throwable);
                        }
                    }));

                    array = buffered.readLine();
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        response.setStatus(HttpServletResponse.SC_OK);
        CompletableFuture.allOf(operations.toArray(new CompletableFuture[operations.size()])).whenComplete((aVoid, throwable) -> {
            if(throwable!=null){
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        });
    }

    /**
     * Get list of current runnable experiments
     *
     * $ curl 127.0.0.1:8080/player/experiments
     */
    @RequestMapping(value = "/experiments", method = RequestMethod.GET)
    public List<Experiment> experiments(){
        ArrayList<Experiment> result = new ArrayList<>();
        experimnetRepository.findAll().forEach(result::add);
        return result;
    }

    /**
     * Play a scenario
     *
     * $ curl "127.0.0.1:8080/player/start?recordingId=572b01abc92e6b697f9d9ab2"
     * $ curl "127.0.0.1:8080/player/start?recordingId=572b01abc92e6b697f9d9ab2&withScreenshots=true&paused=false"
     * $ curl "127.0.0.1:8080/player/start?recordingId=572b01abc92e6b697f9d9ab2&paused=true"
     * @param recordingId id of existing recording
     * @return
     */
    @RequestMapping(value = "/start", method = RequestMethod.GET)
    public Experiment start(@RequestParam("recordingId")String recordingId,
                            @RequestParam(value = "withScreenshots", defaultValue="false") Boolean withScreenshots,
                            @RequestParam(value = "paused", defaultValue="true") Boolean paused) {
        Recording rec = recordingRepository.findOne(new ObjectId(recordingId));
        if(rec==null){
            throw new IllegalArgumentException("no recording found for id "+recordingId);
        }

        Experiment experiment = new Experiment();
        experiment.setCreated(new Date());
        experiment.setRecordingName(rec.getName());
        experiment.setRecordingId(rec.getId());
        experiment.setScreenshots(withScreenshots);

        experiment.getStatus().setPosition(0L);
        experiment.getStatus().setLimit(0L);

        if(!Boolean.TRUE.equals(paused)) {
            experiment.getStatus().setPlaying(true);
        }

        experimnetRepository.save(experiment);
        return experiment;
    }

    /**
     * Get current experiment status: is it played, what is current step
     *
     * @param experimentId
     */
    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public ExperimentStatus status(@RequestParam("experimentId")String experimentId)
    {
        Experiment experiment = experimnetRepository.findOne(new ObjectId(experimentId));

        if(experiment==null){
            throw new IllegalArgumentException("no experiment found for id "+experimentId);
        }

        return experiment.getStatus();
    }

    /**
     * Get screenshot after some step in experiment
     *
     * @param experimentId
     * @param step
     */
    @RequestMapping(value = "/screenshot", method = RequestMethod.GET)
    public void screenshot(@RequestParam("experimentId")String experimentId, @RequestParam("step") Long step)
    {

    }

    /**
     * Skip some steps or go backward in experiment
     *
     * @param experimentId
     * @param step
     */
    @RequestMapping(value = "/move", method = RequestMethod.GET)
    public void move(@RequestParam("experimentId")String experimentId, @RequestParam("step") Long step)
    {

    }

    /**
     * Pause playing the scenario
     *
     * @param experimentId
     */
    @RequestMapping(value = "/pause", method = RequestMethod.GET)
    public void pause(@RequestParam("experimentId")String experimentId)
    {

    }

    /**
     * Continue playing the scenario
     *
     * @param experimentId
     */
    @RequestMapping(value = "/resume", method = RequestMethod.GET)
    public void resume(@RequestParam("experimentId")String experimentId)
    {

    }

    /**
     * Stop playing scenario at all
     * @param experimentId
     */
    @RequestMapping(value = "/cancel", method = RequestMethod.GET)
    public void cancel(@RequestParam("experimentId")String experimentId)
    {

    }

    /**
     * Check if the App can be terminated without any troubles.
     * Checks all opened browsers and determine if them can be freely closed
     * @param experimentId
     */
    @RequestMapping(value = "/terminable", method = RequestMethod.GET)
    public void terminable(@RequestParam("experimentId")String experimentId)
    {

    }

    @RequestMapping(method = RequestMethod.POST, value = "/configure")
    public void configure()
    {

    }
}
