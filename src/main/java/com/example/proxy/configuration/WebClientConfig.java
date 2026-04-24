package com.example.proxy.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import java.net.URI;

@Configuration
public class WebClientConfig {
    private final String proxy;

    public WebClientConfig(@Value("${http.proxy}") String proxy) {
        this.proxy = proxy;
    }
    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .proxy(proxy -> proxy
                                .type(ProxyProvider.Proxy.HTTP)
                                .host(URI.create(this.proxy).getHost())
                                .port(URI.create(this.proxy).getPort())
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
