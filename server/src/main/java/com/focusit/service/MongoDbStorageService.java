package com.focusit.service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import com.focusit.scenario.MongoDbScenario;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Created by doki on 14.05.16.
 */
@Service
public class MongoDbStorageService
{
    @Inject
    MongoDbFactory mongoDbFactory;
    @Inject
    MappingMongoConverter mappingMongoConverter;

    private GridFsTemplate getGridFsTemplate()
    {
        return new GridFsTemplate(mongoDbFactory, mappingMongoConverter);
    }

    public void storeScreenshot(MongoDbScenario scenario, int position, InputStream stream)
    {
        DBObject metaData = new BasicDBObject();
        metaData.put("recordingName", scenario.getRecordingName());
        metaData.put("recordingId", scenario.getRecordingId());
        metaData.put("experimentId", scenario.getExperimentId());
        metaData.put("tag", scenario.getTag());
        metaData.put("tagHash", scenario.getTagHash());

        String dir = scenario.getConfiguration().getCommonConfiguration().getScreenDir() + File.separator
                + Paths.get(scenario.getScenarioFilename()).getFileName().toString();
        String fname = new String(dir.toString() + File.separator + String.format("%05d", position) + ".png");

        getGridFsTemplate().store(stream, fname, "image/png", metaData);
    }

    public void storeJMeterScenario(MongoDbScenario scenario)
    {

    }
}
