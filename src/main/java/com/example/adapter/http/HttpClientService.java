package com.example.adapter.http;

import com.example.adapter.dto.HttpRequestContext;
import com.example.adapter.dto.HttpResponseResult;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class HttpClientService {

    @Value("${adapter.http.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${adapter.http.read-timeout:30000}")
    private int readTimeout;

    @Value("${adapter.http.write-timeout:30000}")
    private int writeTimeout;

    @Value("${adapter.http.max-idle-connections:100}")
    private int maxIdleConnections;

    @Value("${adapter.http.keep-alive-duration:300}")
    private int keepAliveDuration;

    private OkHttpClient httpClient;

    @PostConstruct
    public void init() {
        ConnectionPool connectionPool = new ConnectionPool(
                maxIdleConnections,
                keepAliveDuration,
                TimeUnit.SECONDS
        );

        httpClient = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
                .connectionPool(connectionPool)
                .build();
    }

    public HttpResponseResult execute(HttpRequestContext context) {
        long startTime = System.currentTimeMillis();

        try {
            OkHttpClient client = httpClient;
            if (context.getTimeout() > 0 && context.getTimeout() != readTimeout) {
                client = httpClient.newBuilder()
                        .readTimeout(context.getTimeout(), TimeUnit.MILLISECONDS)
                        .build();
            }

            Request request = buildRequest(context);
            log.info("Sending HTTP request, traceId={}, url={}, method={}",
                    context.getTraceId(), context.getUrl(), context.getMethod());

            try (Response response = client.newCall(request).execute()) {
                long costTime = System.currentTimeMillis() - startTime;
                String responseBody = response.body() != null ? response.body().string() : null;

                log.info("HTTP response received, traceId={}, statusCode={}, costTime={}ms",
                        context.getTraceId(), response.code(), costTime);

                return HttpResponseResult.success(response.code(), responseBody, costTime);
            }

        } catch (IOException e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("HTTP request failed, traceId={}, error={}", context.getTraceId(), e.getMessage(), e);
            return HttpResponseResult.fail(e.getMessage(), costTime);
        }
    }

    private Request buildRequest(HttpRequestContext context) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(context.getUrl()).newBuilder();
        for (Map.Entry<String, String> entry : context.getQueryParams().entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }

        Request.Builder requestBuilder = new Request.Builder()
                .url(urlBuilder.build());

        for (Map.Entry<String, String> entry : context.getHeaders().entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }

        String method = context.getMethod().toUpperCase();
        switch (method) {
            case "GET":
                requestBuilder.get();
                break;
            case "DELETE":
                if (context.getBody() != null && !context.getBody().isEmpty()) {
                    requestBuilder.delete(createRequestBody(context));
                } else {
                    requestBuilder.delete();
                }
                break;
            case "POST":
                requestBuilder.post(createRequestBody(context));
                break;
            case "PUT":
                requestBuilder.put(createRequestBody(context));
                break;
            case "PATCH":
                requestBuilder.patch(createRequestBody(context));
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        return requestBuilder.build();
    }

    private RequestBody createRequestBody(HttpRequestContext context) {
        MediaType mediaType = MediaType.parse(context.getContentType());
        String bodyContent = serializeBody(context);
        return RequestBody.create(bodyContent, mediaType);
    }

    private String serializeBody(HttpRequestContext context) {
        if (context.getBody() == null || context.getBody().isEmpty()) {
            return "{}";
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(context.getBody());
        } catch (Exception e) {
            log.warn("Failed to serialize body, traceId={}", context.getTraceId(), e);
            return "{}";
        }
    }
}
