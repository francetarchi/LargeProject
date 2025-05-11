package com.wineadvisor.wineadvisor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.wineadvisor.wineadvisor.repository")
public class RepositoryConfig {
    @Bean
    /*
     * Configura il MongoTemplate per l'uso con MongoDB, disabilitando l'aggiunta del campo _class nei documenti.
     */
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory, MongoMappingContext context) {
        DefaultDbRefResolver defaultDbRefResolver = new DefaultDbRefResolver(mongoDbFactory);
        MappingMongoConverter converter = new MappingMongoConverter(defaultDbRefResolver, context);

        // Disabilita l'aggiunta del campo _class
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));

        return new MongoTemplate(mongoDbFactory, converter);
    }
}
