package com.filichkin.blog.reactive.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@RestController
public class Controller {
    private static final String USER_SERVICE = "http://ec2-34-201-131-104.compute-1.amazonaws.com:8080/user?delay=100";
    private final HttpClient httpClient = HttpClient.newBuilder().executor(Executors.newFixedThreadPool(8)).build();


//    @GetMapping("user")
//    public String getUser() throws IOException, InterruptedException {
//        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(USER_SERVICE)).build();
//        return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
//    }
    @GetMapping("user")
    public CompletableFuture<String> getUser(){
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(USER_SERVICE)).build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(httpResponse ->httpResponse.body());
    }


}
