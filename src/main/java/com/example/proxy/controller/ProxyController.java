package com.example.proxy.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/proxy")
public class ProxyController {
    private final WebClient webClient;

    public ProxyController(WebClient webClient) {
        this.webClient = webClient;
    }

    @RequestMapping
    public Mono<ResponseEntity<?>> proxy(ServerHttpRequest request) {
        List<String> targetUrl = request.getQueryParams().get("url");

        if (targetUrl == null) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        String url = targetUrl.get(0);

        return webClient
                .method(request.getMethod())
                .uri(URI.create(url))
                .headers(h -> {
                    h.addAll(request.getHeaders());
                    h.remove(HttpHeaders.HOST);
                })
                .body(((outputMessage, context) -> outputMessage.writeWith(request.getBody())))
                .exchangeToMono(resp ->
                        resp.bodyToMono(byte[].class)
                                .map(body -> ResponseEntity
                                        .status(resp.statusCode())
                                        .headers(resp.headers().asHttpHeaders())
                                        .body(body)
                                )
                );
    }
}
