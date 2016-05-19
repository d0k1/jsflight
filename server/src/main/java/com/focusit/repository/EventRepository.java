package com.focusit.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.focusit.model.Event;

/**
 * Created by dkirpichenkov on 04.05.16.
 */
public interface EventRepository extends PagingAndSortingRepository<Event, ObjectId>
{

    @Query(value = "{'recordingId': ?0}", count = true)
    long countByRecordingId(ObjectId recordingId);

    @Query(value = "{'recordingId': ?0}")
    Event findOneByRecordingIdOrderByTimestampDesc(ObjectId recordingId);
}
