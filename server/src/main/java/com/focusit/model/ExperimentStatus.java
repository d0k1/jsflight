package com.focusit.model;

/**
 * Created by dkirpichenkov on 05.05.16.
 */
public class ExperimentStatus {
    private Long position = 0L;

    private Long limit = 0L;

    private Boolean playing;

    private Boolean error;

    private String errorMessage;

    public ExperimentStatus() {
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
}
