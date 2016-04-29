package com.focusit.controllers;

import com.focusit.model.Recording;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dkirpichenkov on 29.04.16.
 */
@RestController
@RequestMapping(value="/player")
public class PlayerController {

    @RequestMapping(value = "/list", method= RequestMethod.GET)
    public List<Recording> getRecordings(){
        return new ArrayList<>();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/upload-scenario")
    public void uploadScenario(@RequestParam("name") String name,
                               @RequestParam("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response){


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
