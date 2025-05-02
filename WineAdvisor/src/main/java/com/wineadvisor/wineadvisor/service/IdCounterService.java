package com.wineadvisor.wineadvisor.service;

import com.wineadvisor.wineadvisor.model.utils.IdCounter;

import lombok.RequiredArgsConstructor;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.stereotype.Service;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
@RequiredArgsConstructor
public class IdCounterService {

    private final MongoOperations mongoOperations;

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

