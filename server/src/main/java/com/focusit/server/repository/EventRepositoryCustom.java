package com.focusit.server.repository;

import java.util.List;

import org.bson.types.ObjectId;

import com.focusit.server.model.Event;

/**
 * Created by dkirpichenkov on 20.05.16.
 */
public interface EventRepositoryCustom
{
    Event getEventToReplay(ObjectId recordingId, int offset);

    long countByRecordingId(ObjectId recordingId);

    void save(List<Event> lineEvents);
}
