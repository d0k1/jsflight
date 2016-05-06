package com.focusit.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Playing process state
 *
 * Created by dkirpichenkov on 29.04.16.
 */
@Document
public class Experiment {

    @Id
    private ObjectId id;

    @Indexed
    private Date created;

    @Indexed
    private ObjectId recordingId;

    @Indexed
    private String recordingName;

    private String tag;

    private String tagHash;

    private String selectQuery = "";

    private Boolean screenshots = false;

    private ExperimentStatus status = new ExperimentStatus();

    public String getId() {
        return id.toString();
    }

    public void setId(String id) {
        this.id = new ObjectId(id);
    }

    public String getRecordingId() {
        return recordingId.toString();
    }

    public void setRecordingId(String recordingId) {
        this.recordingId = new ObjectId(recordingId);
    }

    public String getRecordingName() {
        return recordingName;
    }

    public void setRecordingName(String recordingName) {
        this.recordingName = recordingName;
    }

    public String getSelectQuery() {
        return selectQuery;
    }

    public void setSelectQuery(String selectQuery) {
        this.selectQuery = selectQuery;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Boolean getScreenshots() {
        return screenshots;
    }

    public void setScreenshots(Boolean screenshots) {
        this.screenshots = screenshots;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setRecordingId(ObjectId recordingId) {
        this.recordingId = recordingId;
    }

    public ExperimentStatus getStatus() {
        return status;
    }

    public void setStatus(ExperimentStatus status) {
        this.status = status;
    }
}
