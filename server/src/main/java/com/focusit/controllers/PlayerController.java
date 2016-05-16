package com.focusit.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.focusit.model.Experiment;
import com.focusit.model.Recording;
import com.focusit.player.BackgroundWebPlayer;
import com.focusit.service.MongoDbStorageService;
import com.focusit.service.RecordingsService;

/**
 * Created by dkirpichenkov on 29.04.16.
 */
@RestController
@RequestMapping(value = "/player")
public class PlayerController
{

    private final static Logger LOG = LoggerFactory.getLogger(PlayerController.class);
    private RecordingsService recordingsService;
    private BackgroundWebPlayer player;
    private MongoDbStorageService storageService;

    @Inject
    public PlayerController(RecordingsService recordingsService, BackgroundWebPlayer player,
            MongoDbStorageService storageService)
    {
        this.recordingsService = recordingsService;
        this.player = player;
        this.storageService = storageService;
    }

    /**
     * Get list of uploaded scenarios
     *
     *
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public List<Recording> getRecordings()
    {
        return recordingsService.getAllRecordings();
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
    public void uploadScenario(@RequestParam("name") String name, @RequestParam("file") MultipartFile file,
            HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        response.setStatus(HttpServletResponse.SC_OK);
        if (!recordingsService.importRecording(name, file.getInputStream()))
        {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get list of current runnable experiments
     *
     * $ curl 127.0.0.1:8080/player/experiments
     */
    @RequestMapping(value = "/experiments", method = RequestMethod.GET)
    public List<Experiment> experiments()
    {
        return player.getAllExperiments();
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
    public Experiment start(@RequestParam("recordingId") String recordingId,
            @RequestParam(value = "withScreenshots", defaultValue = "false") Boolean withScreenshots,
            @RequestParam(value = "paused", defaultValue = "true") Boolean paused)
    {
        return player.start(recordingId, withScreenshots, paused);
    }

    /**
     * Get current experiment status: is it played, what is current step
     *
     * @param experimentId
     */
    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public Experiment status(@RequestParam("experimentId") String experimentId)
    {
        return player.status(experimentId);
    }

    /**
     * Get screenshot after some step in experiment
     *
     * @param experimentId
     * @param step
     */
    @RequestMapping(value = "/screenshot", method = RequestMethod.GET)
    public void screenshot(@RequestParam("experimentId") String experimentId, @RequestParam("step") Integer step,
            HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        InputStream stream = player.getScreenshot(experimentId, step);
        if (stream == null)
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("image/png");
        IOUtils.copy(stream, response.getOutputStream());
        response.flushBuffer();
    }

    /**
     * Skip some steps or go backward in experiment
     *
     * @param experimentId
     * @param step
     */
    @RequestMapping(value = "/move", method = RequestMethod.GET)
    public void move(@RequestParam("experimentId") String experimentId, @RequestParam("step") Integer step)
    {
        player.move(experimentId, step);
    }

    /**
     * Pause playing the scenario
     *
     * @param experimentId
     */
    @RequestMapping(value = "/pause", method = RequestMethod.GET)
    public void pause(@RequestParam("experimentId") String experimentId)
    {
        player.pause(experimentId);
    }

    /**
     * Continue playing the scenario
     *
     * @param experimentId
     */
    @RequestMapping(value = "/resume", method = RequestMethod.GET)
    public void resume(@RequestParam("experimentId") String experimentId)
    {
        player.resume(experimentId);
    }

    /**
     * Stop playing scenario at all
     * @param experimentId
     */
    @RequestMapping(value = "/cancel", method = RequestMethod.GET)
    public void cancel(@RequestParam("experimentId") String experimentId)
    {
        player.cancel(experimentId);
    }

    /**
     * Check if the App can be terminated without any troubles.
     * Checks all opened browsers and determine if them can be freely closed
     * @param experimentId
     */
    @RequestMapping(value = "/terminable", method = RequestMethod.GET)
    public void terminable(@RequestParam("experimentId") String experimentId)
    {

    }

    @RequestMapping(method = RequestMethod.POST, value = "/configure")
    public void configure(@RequestParam("experimentId") String experimentId, HttpServletRequest request)
    {

    }
}
