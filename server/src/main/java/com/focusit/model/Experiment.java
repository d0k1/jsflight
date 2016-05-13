package com.focusit.model;

import com.focusit.jsflight.player.config.Configuration;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

    private Configuration configuration;

    private String tag;

    private String tagHash;

    private String selectQuery = "";

    private Boolean screenshots = false;

    private Long position = 0L;

    private Long limit = 0L;

    private Long steps = 0L;

    private Boolean playing=false;

    private Boolean error=false;

    private String errorMessage="";

    public String getId() {
        return id.toString();
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setId(String id) {
        this.id = new ObjectId(id);
    }

    public String getRecordingId() {
        return recordingId.toString();
    }

    public void setRecordingId(ObjectId recordingId) {
        this.recordingId = recordingId;
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

    @SuppressFBWarnings("EI_EXPOSE_REP")
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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTagHash() {
        return tagHash;
    }

    public void setTagHash(String tagHash) {
        this.tagHash = tagHash;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public Long getPosition() {
        return position;
    }

    public void setPosition(Long position) {
        this.position = position;
    }

    public Long getLimit() {
        return limit;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public Boolean getPlaying() {
        return playing;
    }

    public void setPlaying(Boolean playing) {
        this.playing = playing;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getSteps() {
        return steps;
    }

    public void setSteps(Long steps) {
        this.steps = steps;
    }
}
