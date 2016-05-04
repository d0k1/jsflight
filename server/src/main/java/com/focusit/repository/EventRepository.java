package com.focusit.repository;

import com.focusit.model.Event;
import org.bson.types.ObjectId;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by dkirpichenkov on 04.05.16.
 */
public interface EventRepository extends PagingAndSortingRepository<Event, ObjectId> {
}
