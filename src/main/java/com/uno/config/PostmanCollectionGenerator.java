package com.uno.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PostmanCollectionGenerator {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final Environment environment;

    @Value("${spring.application.name}")
    private String appName;

    @Autowired
    public PostmanCollectionGenerator(RequestMappingHandlerMapping requestMappingHandlerMapping, Environment environment) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.environment = environment;
    }

    public void generatePostmanCollection() {
        String outputPath = "src/main/resources/postman_collection.json";
        try {
            Map<String, Object> postmanCollection = new HashMap<>();
            postmanCollection.put("info", Map.of(
                    "_postman_id", UUID.randomUUID().toString(),
                    "name", appName != null ? appName : "Postman_Collection",
                    "schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
            ));

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

            // Group endpoints by controller
            Map<Class<?>, List<EndpointInfo>> controllerEndpoints = groupEndpointsByController();

            // Create folders based on controllers
            List<Map<String, Object>> folderItems = new ArrayList<>();

            for (Map.Entry<Class<?>, List<EndpointInfo>> entry : controllerEndpoints.entrySet()) {
                Class<?> controllerClass = entry.getKey();
                List<EndpointInfo> endpoints = entry.getValue();

                // Extract domain name from controller name
                String controllerName = controllerClass.getSimpleName();
                String domainName = extractDomainName(controllerName);

                // Check if controller has @RequestMapping with a base path
                String basePath = "";
                if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping mapping = controllerClass.getAnnotation(RequestMapping.class);
                    if (mapping.value().length > 0) {
                        basePath = mapping.value()[0];
                    }
                }

                List<Map<String, Object>> endpointItems = new ArrayList<>();

                for (EndpointInfo endpoint : endpoints) {
                    String pattern = endpoint.getPattern();
                    if (basePath.length() > 0 && pattern.startsWith(basePath)) {
                        pattern = pattern.substring(basePath.length());
                    }

                    Map<String, Object> requestDetails = createRequestDetails(
                            endpoint.getPattern(),
                            endpoint.getMethod(),
                            endpoint.getHandlerMethod(),
                            objectMapper
                    );

                    if (requestDetails != null) {
                        // Create a descriptive name that includes both the controller name and method
                        String methodName = endpoint.getHandlerMethod().getMethod().getName();
                        String endpointName = controllerClass.getSimpleName() + " - " + methodName;

                        // Add HTTP method and path to the name for better clarity
                        endpointName += " [" + endpoint.getMethod() + "] " + pattern;

                        endpointItems.add(Map.of(
                                "name", endpointName,
                                "request", requestDetails
                        ));
                    }
                }

                // Create folder for domain with its endpoints
                // Check if this domain already exists in our folders
                Optional<Map<String, Object>> existingFolder = folderItems.stream()
                        .filter(folder -> folder.get("name").equals(domainName))
                        .findFirst();

                if (existingFolder.isPresent()) {
                    // Add endpoints to existing folder
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> existingItems = (List<Map<String, Object>>) existingFolder.get().get("item");
                    existingItems.addAll(endpointItems);
                } else {
                    // Create new folder for this domain
                    folderItems.add(Map.of(
                            "name", domainName,
                            "item", endpointItems
                    ));
                }
            }

            postmanCollection.put("item", folderItems);

            File file = new File(outputPath);
            objectMapper.writeValue(file, postmanCollection);
            System.out.println("Postman collection created successfully: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error while creating file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Extracts the domain name from a controller name
     * For example: RestaurantController -> Restaurant, UserProfileController -> User Profile
     */
    private String extractDomainName(String controllerName) {
        // Remove common suffixes
        String domain = controllerName;
        if (domain.endsWith("Controller")) {
            domain = domain.substring(0, domain.length() - "Controller".length());
        } else if (domain.endsWith("Resource")) {
            domain = domain.substring(0, domain.length() - "Resource".length());
        } else if (domain.endsWith("RestController")) {
            domain = domain.substring(0, domain.length() - "RestController".length());
        } else if (domain.endsWith("Api")) {
            domain = domain.substring(0, domain.length() - "Api".length());
        }

        // Insert spaces between words (camel case to space separated)
        domain = domain.replaceAll("([a-z])([A-Z])", "$1 $2");

        return domain;
    }

    private Map<Class<?>, List<EndpointInfo>> groupEndpointsByController() {
        Map<Class<?>, List<EndpointInfo>> controllerEndpoints = new HashMap<>();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : requestMappingHandlerMapping.getHandlerMethods().entrySet()) {
            RequestMappingInfo mappingInfo = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();

            Class<?> controllerClass = handlerMethod.getBeanType();

            // Skip if not a controller or RestController
            if (!controllerClass.isAnnotationPresent(Controller.class) &&
                    !controllerClass.isAnnotationPresent(RestController.class)) {
                continue;
            }

            Set<String> patterns = getPatterns(mappingInfo);
            if (patterns == null || patterns.isEmpty()) {
                continue;
            }

            Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();
            if (methods == null || methods.isEmpty()) {
                continue;
            }

            for (String pattern : patterns) {
                for (RequestMethod method : methods) {
                    controllerEndpoints
                            .computeIfAbsent(controllerClass, k -> new ArrayList<>())
                            .add(new EndpointInfo(pattern, method, handlerMethod));
                }
            }
        }

        return controllerEndpoints;
    }

    private Set<String> getPatterns(RequestMappingInfo mappingInfo) {
        Set<String> patterns = null;
        if (mappingInfo.getPatternsCondition() != null) {
            patterns = mappingInfo.getPatternsCondition().getPatterns();
        } else if (mappingInfo.getPathPatternsCondition() != null) {
            patterns = mappingInfo.getPathPatternsCondition().getPatternValues();
        }
        return patterns != null ? patterns : Collections.emptySet();
    }

    private Map<String, Object> createRequestDetails(String pattern, RequestMethod method, HandlerMethod handlerMethod, ObjectMapper objectMapper) throws IOException {
        Map<String, Object> requestDetails = new HashMap<>();
        requestDetails.put("method", method.name());
        requestDetails.put("header", Collections.emptyList());

        String port = environment.getProperty("local.server.port", "8080");

        List<Map<String, String>> queryParameters = new ArrayList<>();
        StringBuilder rawUrl = new StringBuilder("http://localhost:" + port + pattern);

        Map<String, Object> bodyContent = new HashMap<>();
        boolean hasRequestBody = false;

        for (Parameter parameter : handlerMethod.getMethod().getParameters()) {
            if (parameter.isAnnotationPresent(RequestBody.class)) {
                hasRequestBody = true;
                bodyContent.putAll(generateExampleJson(parameter.getType()));
            } else if (parameter.isAnnotationPresent(RequestParam.class)) {
                RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                String paramName = requestParam.value().isEmpty() ? parameter.getName() : requestParam.value();
                queryParameters.add(Map.of("key", paramName, "value", "sampleValue"));
            } else if (parameter.isAnnotationPresent(PathVariable.class)) {
                String paramName = parameter.getName();
                pattern = pattern.replace("{" + paramName + "}", "{" + paramName + "}");
            }
        }

        if (!queryParameters.isEmpty()) {
            rawUrl.append("?")
                    .append(String.join("&", queryParameters.stream()
                            .map(param -> param.get("key") + "=" + param.get("value"))
                            .toArray(String[]::new)));
        }

        // Split path correctly for Postman
        List<String> pathSegments = Arrays.stream(pattern.split("/"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        requestDetails.put("url", Map.of(
                "raw", rawUrl.toString(),
                "protocol", "http",
                "host", List.of("localhost"),
                "port", port,
                "path", pathSegments,
                "query", queryParameters
        ));

        if (hasRequestBody) {
            requestDetails.put("body", Map.of(
                    "mode", "raw",
                    "raw", objectMapper.writeValueAsString(bodyContent),
                    "options", Map.of("raw", Map.of("language", "json"))
            ));
        } else {
            requestDetails.put("body", Map.of("mode", "none"));
        }

        return requestDetails;
    }

    private Map<String, Object> generateExampleJson(Class<?> clazz) {
        Map<String, Object> exampleJson = new HashMap<>();

        for (var field : clazz.getDeclaredFields()) {
            if (field.getType().equals(String.class)) {
                exampleJson.put(field.getName(), "example");
            } else if (field.getType().equals(Long.class) || field.getType().equals(Long.TYPE)) {
                exampleJson.put(field.getName(), 0L);
            } else if (field.getType().equals(Integer.class) || field.getType().equals(Integer.TYPE)) {
                exampleJson.put(field.getName(), 0);
            } else if (field.getType().equals(Boolean.class) || field.getType().equals(Boolean.TYPE)) {
                exampleJson.put(field.getName(), false);
            } else if (field.getType().equals(Double.class) || field.getType().equals(Double.TYPE)) {
                exampleJson.put(field.getName(), 0.0);
            } else {
                exampleJson.put(field.getName(), "nestedObject");
            }
        }
        return exampleJson;
    }

    // Helper class to store endpoint information
    private static class EndpointInfo {
        private final String pattern;
        private final RequestMethod method;
        private final HandlerMethod handlerMethod;

        public EndpointInfo(String pattern, RequestMethod method, HandlerMethod handlerMethod) {
            this.pattern = pattern;
            this.method = method;
            this.handlerMethod = handlerMethod;
        }

        public String getPattern() {
            return pattern;
        }

        public RequestMethod getMethod() {
            return method;
        }

        public HandlerMethod getHandlerMethod() {
            return handlerMethod;
        }
    }
}