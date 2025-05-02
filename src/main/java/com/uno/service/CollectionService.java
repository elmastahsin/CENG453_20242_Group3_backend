package com.uno.service;


import com.uno.config.PostmanCollectionGenerator;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Service
public class CollectionService {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final Environment environment;

    @Autowired
    public CollectionService(RequestMappingHandlerMapping requestMappingHandlerMapping, Environment environment) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.environment = environment;

    }

    @PostConstruct
    public void init() {
        PostmanCollectionGenerator postmanCollectionGenerator = new PostmanCollectionGenerator(requestMappingHandlerMapping, environment);
        postmanCollectionGenerator.generatePostmanCollection();
    }
}