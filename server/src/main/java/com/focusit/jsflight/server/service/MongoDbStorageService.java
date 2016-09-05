package com.focusit.jsflight.server.service;

import com.focusit.jsflight.server.scenario.MongoDbScenario;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.InputStream;

@Service
public class MongoDbStorageService
{
    public static final String POSITION_FORMAT = "%06d";
    private static final Logger LOG = LoggerFactory.getLogger(MongoDbStorageService.class);
    @Inject
    MongoDbFactory mongoDbFactory;
    @Inject
    MappingMongoConverter mappingMongoConverter;

    private GridFsTemplate getGridFsTemplate()
    {
        return new GridFsTemplate(mongoDbFactory, mappingMongoConverter);
    }

    public void storeScreenshot(MongoDbScenario scenario, int position, InputStream stream, boolean error)
    {
        LOG.info("Making screenshot");
        DBObject metaData = new BasicDBObject();
        metaData.put("recordingName", scenario.getRecordingName());
        metaData.put("recordingId", scenario.getRecordingId());
        metaData.put("experimentId", scenario.getExperimentId());
        metaData.put("tag", scenario.getTag());
        metaData.put("tagHash", scenario.getTagHash());

        String filename = error
                ? createErrorImageName(scenario.getRecordingName(), scenario.getExperimentId(), position)
                : createImageName(scenario.getRecordingName(), scenario.getExperimentId(), position);

        getGridFsTemplate().store(stream, filename, "image/png", metaData);
    }

    public InputStream getScreenshot(String recordingName, String experimentId, int step)
    {
        return getStreamByFilename(createImageName(recordingName, experimentId, step));
    }

    public InputStream getErrorScreenShot(String recordingName, String experimentId, int step)
    {
        return getStreamByFilename(createErrorImageName(recordingName, experimentId, step));
    }

    @Nullable
    private InputStream getStreamByFilename(String fname)
    {
        GridFSDBFile file = getGridFsTemplate().findOne(new Query().addCriteria(Criteria.where("filename").is(fname)));
        if (file != null)
        {
            return file.getInputStream();
        }
        return null;
    }

    public void storeJMeterScenario(MongoDbScenario scenario, InputStream stream)
    {
        DBObject metaData = new BasicDBObject();
        metaData.put("recordingName", scenario.getRecordingName());
        metaData.put("recordingId", scenario.getRecordingId());
        metaData.put("experimentId", scenario.getExperimentId());
        metaData.put("tag", scenario.getTag());
        metaData.put("tagHash", scenario.getTagHash());

        String fname = createJmxName(scenario.getScenarioFilename(), scenario.getExperimentId());
        getGridFsTemplate().store(stream, fname, "text/xml", metaData);
    }

    public InputStream getJMeterScenario(String recordingName, String experimentId)
    {
        return getStreamByFilename(createJmxName(recordingName, experimentId));
    }

    private String createJmxName(String recordingName, String experimentId) {
        return recordingName + "_" + experimentId + ".jmx";
    }

    private String createImageName(String recordingName, String experimentId, int position)
    {
        return String.format("%s_%s_%s.png", recordingName, experimentId,
                String.format(POSITION_FORMAT, position));
    }

    private String createErrorImageName(String recordingName, String experimentId, int position)
    {
        return String.format("%s_%s_error_%s.png", recordingName, experimentId,
                String.format(POSITION_FORMAT, position));
    }
}
