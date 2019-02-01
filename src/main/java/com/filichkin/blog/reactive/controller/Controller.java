package com.filichkin.blog.reactive.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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


    @Autowired
    public Controller(@Value("${user.service.host}") String userServiceHost) {
        this.userServiceHost = userServiceHost;
        this.webClient = WebClient.builder().baseUrl(userServiceHost).build();
        this.httpClient = HttpClient.newBuilder().executor(Executors.newFixedThreadPool(10)).build();
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

    private CompletableFuture<String> sendRequestWithHttpClient(long delay) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/user/?delay=%d", userServiceHost, delay)))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
}
