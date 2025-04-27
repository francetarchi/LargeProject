package com.wineadvisor.wineadvisor.service;

import com.wineadvisor.wineadvisor.model.utils.IdCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.stereotype.Service;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class IdCounterService {

    @Autowired
    private MongoOperations mongoOperations;

    public Long generateSequence(String seqName) {
        IdCounter counter = mongoOperations.findAndModify(
            Query.query(where("_id").is(seqName)),
            new Update().inc("seq", 1),
            FindAndModifyOptions.options().returnNew(true).upsert(true),
            IdCounter.class
        );

        return (counter != null) ? counter.getSeq() : 1;
    }
}

