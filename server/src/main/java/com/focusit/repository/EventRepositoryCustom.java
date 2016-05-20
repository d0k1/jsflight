package com.focusit.repository;

import org.bson.types.ObjectId;

import com.focusit.model.Event;

/**
 * Created by dkirpichenkov on 20.05.16.
 */
public interface EventRepositoryCustom
{
    Event findOneByRecordingIdOrderByTimestampAsc(ObjectId recordingId, int offset);
}
