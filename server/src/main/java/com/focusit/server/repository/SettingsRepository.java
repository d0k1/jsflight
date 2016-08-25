package com.focusit.server.repository;

import com.focusit.server.model.Settings;
import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by doki on 30.04.16.
 */
public interface SettingsRepository extends CrudRepository<Settings, ObjectId>
{

    @Override
    Settings findOne(ObjectId objectId);

}
