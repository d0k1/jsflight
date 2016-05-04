package com.focusit.service;

import com.focusit.model.Settings;
import com.focusit.repository.SettingsRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.focusit.model.Settings.SETTINGS_ID;

/**
 * Created by doki on 30.04.16.
 */
@Service
public class SettingsService {
    @Inject
    private SettingsRepository repository;
    private volatile Settings settings;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    @PostConstruct
    public void init()
    {
        lock.writeLock().lock();
        try{
            Settings settings = repository.findOne(new ObjectId(SETTINGS_ID));
            if(settings==null){
                System.err.println("No settings found. Creating default");
                settings = new Settings();
                repository.save(settings);
            }
            this.settings = settings;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Settings getSettings(){
        lock.readLock().lock();
        try{
            return settings;
        } finally {
            lock.readLock().unlock();
        }
    }
}
