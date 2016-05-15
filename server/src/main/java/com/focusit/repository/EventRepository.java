package com.focusit.repository;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.focusit.model.Event;

/**
 * Created by dkirpichenkov on 04.05.16.
 */
public interface EventRepository extends PagingAndSortingRepository<Event, ObjectId>
{

    Long countByRecordingId(String recordingId);

    Page<Event> findOneByRecordingId(String recordingId, Pageable pageable);
}
