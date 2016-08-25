package com.focusit.jsflight.server.repository;

import com.focusit.jsflight.server.model.Experiment;
import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by dkirpichenkov on 05.05.16.
 */
public interface ExperimentRepository extends CrudRepository<Experiment, ObjectId>
{
}
