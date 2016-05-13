package com.focusit.service;

import javax.inject.Inject;

import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

/**
 * Created by doki on 14.05.16.
 */
@Service
public class ScreenshotsService {
    @Inject
    MongoDbFactory mongoDbFactory;
    @Inject
    MappingMongoConverter mappingMongoConverter;

    public GridFsTemplate getGridFsTemplate(){
        return new GridFsTemplate(mongoDbFactory, mappingMongoConverter);
    }
}
