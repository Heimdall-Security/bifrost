package com.heimdallauth.server.services.mongo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MongoDBIndexOps {
    private final MongoTemplate mongoTemplate;

    public MongoDBIndexOps(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    public void ensureCreateMongoDBIndex(Class<?> documentClass){
        log.info("Create MongoDB Index for "+documentClass.getSimpleName());
        MongoMappingContext mappingContext =(MongoMappingContext) mongoTemplate.getConverter().getMappingContext();
        MongoPersistentEntityIndexResolver indexResolver = new MongoPersistentEntityIndexResolver(mappingContext);
        IndexOperations indexOps = mongoTemplate.indexOps(documentClass);
        indexResolver.resolveIndexFor(documentClass).forEach(indexOps::ensureIndex);
    }
}
