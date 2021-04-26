package uk.gov.hmcts.reform.waworkflowapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class RestApiActions {

    private final String baseUri;
    private final PropertyNamingStrategy propertyNamingStrategy;
    RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder();
    RequestSpecification specification;

    public RestApiActions(final String baseUri, final PropertyNamingStrategy propertyNamingStrategy) {
        this.baseUri = baseUri;
        this.propertyNamingStrategy = propertyNamingStrategy;
    }

    public RestApiActions setUp() {
        requestSpecBuilder.setBaseUri(baseUri);
        specification = requestSpecBuilder.build();
        return this;
    }

    protected RequestSpecification given() {
        return RestAssured.given()
            .spec(specification)
            .config(RestAssured.config()
                .objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                    (type, s) -> {
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.setPropertyNamingStrategy(propertyNamingStrategy);
                        objectMapper.registerModule(new Jdk8Module());
                        objectMapper.registerModule(new JavaTimeModule());
                        return objectMapper;
                    }
                ))
            ).relaxedHTTPSValidation();
    }

    public Response get(String path, Header header) {
        return this.get(
            path,
            null,
            APPLICATION_JSON_VALUE,
            APPLICATION_JSON_VALUE,
            new Headers(header),
            emptyMap());
    }

    public Response get(String path, String resourceId, Headers headers) {
        return this.get(path, resourceId, APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE, headers, emptyMap());
    }

    public Response get(String path, Headers headers, Map<String, String> params) {
        return this.get(path, null, APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE, headers, params);
    }

    public Response get(String path, String resourceId, String accept, Headers headers) {
        return this.get(path, resourceId, APPLICATION_JSON_VALUE, accept, headers, emptyMap());
    }


    public Response get(String path,
                        String resourceId,
                        String contentType,
                        String accept,
                        Headers headers,
                        Map<String, String> params) {

        return params.isEmpty()
            ? getWithoutParams(path, resourceId, contentType, accept, headers)
            : getWithParams(path, resourceId, contentType, accept, headers, params);
    }

    public Response getWithoutParams(String path,
                                     String resourceId,
                                     String contentType,
                                     String accept,
                                     Headers headers) {

        if (resourceId != null) {
            log.info("Calling GET {} with resource id: {}", path, resourceId);
            return given()
                .contentType(contentType)
                .accept(accept)
                .headers(headers)
                .when()
                .get(path, resourceId);
        } else {
            log.info("Calling GET {}", path);
            return given()
                .contentType(contentType)
                .accept(accept)
                .headers(headers)
                .when()
                .get(path);
        }
    }

    public Response getWithParams(String path,
                                  String resourceId,
                                  String contentType,
                                  String accept,
                                  Headers headers,
                                  Map<String, String> params) {

        if (resourceId != null) {
            log.info("Calling GET {} with resource id: {} and params '{}'", path, resourceId, params);
            return given()
                .contentType(contentType)
                .accept(accept)
                .headers(headers)
                .params(params)
                .when()
                .get(path, resourceId);
        } else {
            log.info("Calling GET {} with params '{}'", path, params);
            return given()
                .contentType(contentType)
                .accept(accept)
                .headers(headers)
                .params(params)
                .when()
                .get(path);
        }
    }

    public Response post(String path, String resourceId, Headers headers) {
        return post(path, resourceId, null, APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE, headers);
    }

    public Response post(String path, String resourceId, Object body, Header header) {
        return post(path, resourceId, body, APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE, new Headers(header));
    }

    public Response post(String path, String resourceId, Object body, Headers headers) {
        return post(path, resourceId, body, APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE, headers);
    }

    public Response post(String path, Object body, Header header) {
        return post(path, null, body, APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE, new Headers(header));
    }

    public Response post(String path, Object body, Headers headers) {
        return post(path, null, body, APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE, headers);
    }

    public Response post(String path,
                         String resourceId,
                         Object body,
                         String contentType,
                         String accept,
                         Headers headers) {
        return (body != null)
            ? postWithBody(path, resourceId, body, contentType, accept, headers)
            : postWithoutBody(path, resourceId, contentType, accept, headers);
    }

    private Response postWithBody(String path,
                                  String resourceId,
                                  Object body,
                                  String contentType,
                                  String accept,
                                  Headers headers) {
        if (resourceId != null) {
            log.info("Calling POST {} with resource id: {}", path, resourceId);
            return given()
                .contentType(contentType)
                .accept(accept)
                .headers(headers)
                .body(body)
                .when()
                .post(path, resourceId);
        } else {
            log.info("Calling POST {}", path);
            return given()
                .contentType(contentType)
                .accept(accept)
                .headers(headers)
                .body(body)
                .when()
                .post(path);
        }
    }

    private Response postWithoutBody(String path,
                                     String resourceId,
                                     String contentType,
                                     String accept,
                                     Headers headers) {
        if (resourceId != null) {
            log.info("Calling POST {} with resource id: {}", path, resourceId);

            return given()
                .contentType(contentType)
                .accept(accept)
                .headers(headers)
                .when()
                .post(path, resourceId);
        } else {
            log.info("Calling POST {}", path);
            return given()
                .contentType(contentType)
                .accept(accept)
                .headers(headers)
                .when()
                .post(path);
        }
    }

}
