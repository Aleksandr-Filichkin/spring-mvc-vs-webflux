package com.filichkin.blog.reactive.controller;

import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@RestController
public class Controller {
    private final WebClient webClient;
    private final HttpClient httpClient;
    private final String userServiceHost;
    private final CloseableHttpAsyncClient apacheClient;

    @Autowired
    public Controller(@Value("${user.service.host}") String userServiceHost) {
        this.userServiceHost = userServiceHost;
        this.webClient = WebClient.builder().baseUrl(userServiceHost).build();
        this.httpClient = HttpClient.newBuilder().executor(Executors.newSingleThreadExecutor()).build();
        this.apacheClient = HttpAsyncClients.custom().setMaxConnPerRoute(2000).setMaxConnTotal(2000).build();
        this.apacheClient.start();
    }

    @GetMapping(value = "/sync")
    public String getUserSync(@RequestParam long delay) {
        return sendRequestWithHttpClient(delay).thenApply(x -> "sync: " + x).join();
    }


    @GetMapping(value = "/completable-future")
    public CompletableFuture<String> getUserUsingWithCF(@RequestParam long delay) {
        return sendRequestWithHttpClient(delay).thenApply(x -> "completable-future: " + x);
    }

    @GetMapping(value = "/webflux-java-http-client")
    public Mono<String> getUserUsingWebfluxJavaHttpClient(@RequestParam long delay) {
        CompletableFuture<String> stringCompletableFuture = sendRequestWithHttpClient(delay).thenApply(x -> "webflux-java-http-client: " + x);
        return Mono.fromFuture(stringCompletableFuture);
    }

    @GetMapping(value = "/webflux-webclient")
    public Mono<String> getUserUsingWebfluxWebclient(@RequestParam long delay) {
        return webClient.get().uri("/user/?delay={delay}", delay).retrieve().bodyToMono(String.class).map(x -> "webflux-webclient: " + x);
    }

    @GetMapping(value = "/webflux-apache-client")
    public Mono<String> apache(@RequestParam long delay) {
        CompletableFuture<org.apache.http.HttpResponse> cf = new CompletableFuture<>();
        FutureCallback<org.apache.http.HttpResponse> callback = new HttpResponseCallback(cf);
        HttpUriRequest request = new HttpGet(userServiceHost+"/user/?delay="+delay);
        apacheClient.execute(request, callback);
        return Mono.fromCompletionStage(cf.thenApply(response -> {
            try {
                return "apache: " + EntityUtils.toString(response.getEntity());
            } catch (ParseException | IOException e) {
                return e.toString();
            }
        }).exceptionally(Throwable::toString));
    }

    private CompletableFuture<String> sendRequestWithHttpClient(long delay) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/user/?delay=%d", userServiceHost, delay)))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
}


class HttpResponseCallback implements FutureCallback<org.apache.http.HttpResponse> {

    private CompletableFuture<org.apache.http.HttpResponse> cf;

    HttpResponseCallback(CompletableFuture<org.apache.http.HttpResponse> cf) {
        this.cf = cf;
    }

    @Override
    public void failed(Exception ex) {
        cf.completeExceptionally(ex);
    }

    @Override
    public void completed(org.apache.http.HttpResponse result) {
        cf.complete(result);
    }

    @Override
    public void cancelled() {
        cf.completeExceptionally(new Exception("Cancelled by http async client"));
    }
}
