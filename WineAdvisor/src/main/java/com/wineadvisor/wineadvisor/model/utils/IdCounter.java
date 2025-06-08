package com.wineadvisor.wineadvisor.model.utils;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "counters")
public class IdCounter {
    @Id
    @Field("_id")
    private String id;
    private Long seq;
}
