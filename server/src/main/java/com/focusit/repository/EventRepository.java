package com.focusit.repository;

import com.focusit.model.Event;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by dkirpichenkov on 04.05.16.
 */
public interface EventRepository extends PagingAndSortingRepository<Event, ObjectId> {

    Long countByRecordingId(String recordingId);
    Page<Event> findOneByRecordingId(String recordingId, Pageable pageable);
}
