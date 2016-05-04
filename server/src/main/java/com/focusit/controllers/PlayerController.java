package com.focusit.controllers;

import com.focusit.model.Event;
import com.focusit.model.Recording;
import com.focusit.repository.EventRepository;
import com.focusit.repository.RecordingRepository;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
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
import java.util.List;

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

    @RequestMapping(value = "/list", method= RequestMethod.GET)
    public List<Recording> getRecordings(){
        return new ArrayList<>();
    }

    /**
     * example uploading scenario
     * $ curl -F "name=test.json" -F "file=@./test.json" 127.0.0.1:8080/player/upload-scenario
     * @param name
     * @param file
     * @param request
     * @param response
     */
    @RequestMapping(method = RequestMethod.POST, value = "/upload-scenario")
    public void uploadScenario(@RequestParam("name") String name,
                               @RequestParam("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response){


        String array = "";
        List<Event> lineEvents = null;

        Recording rec = recordingRepository.findByName(name);
        if(rec==null){
            rec = new Recording();
            rec.setName(name);
            rec = recordingRepository.save(rec);
        }

        try(InputStreamReader reader = new InputStreamReader(file.getInputStream(), "UTF-8")){
            try(BufferedReader buffered = IOUtils.toBufferedReader(reader)){
                Gson gson = new Gson();
                array = buffered.readLine();
                while(array!=null) {
                    JSONArray rawevents = new JSONArray(array);
                    lineEvents = new ArrayList<>(rawevents.length());

                    for (int i = 0; i < rawevents.length(); i++) {
                        JSONObject event = new JSONObject(rawevents.get(i).toString());
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
                    array = buffered.readLine();
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Recording finalRec = rec;
        lineEvents.forEach(event->{
            event.setRecordingId(finalRec.getId());
            event.setRecordName(finalRec.getName());
        });
        eventRepository.save(lineEvents);

        response.setStatus(HttpServletResponse.SC_OK);
    }

    @RequestMapping(value = "/select")
    public void selectScenario(@RequestParam("name") String name, HttpServletRequest request, HttpServletResponse response){
        response.setStatus(HttpServletResponse.SC_OK);
    }

    public void playScenario(){

    }

    public void getScenarioPosition(){

    }

    public void getScenarioScreenshot(){

    }

    public void resumeScenario(){

    }

    public void setScenarioPosition(){

    }

    public void stopScenario(){

    }

    public void pauseScenario(){

    }
}
