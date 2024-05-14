package com.paymentchain.transaction.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.paymentchain.transaction.entities.Transaction;
import com.paymentchain.transaction.repository.TransactionRepository;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/transaction")
public class TransactionRestController {

    @Autowired
    TransactionRepository transactionRepository;

    private final WebClient.Builder webClientBuilder;

    HttpClient client = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(EpollChannelOption.TCP_KEEPIDLE, 300)
            .option(EpollChannelOption.TCP_KEEPINTVL, 60)
            .responseTimeout(Duration.ofSeconds(1))
            .doOnConnected(connection -> {
                connection.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
                connection.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
            });

    public TransactionRestController(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @GetMapping()
    public List<Transaction> list(){
        return transactionRepository.findAll();
    }

    @GetMapping("/{id}")
    public Transaction get(@PathVariable("id") long id) {
        return transactionRepository.findById(id).get();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> put(@PathVariable ("id") long id, @RequestBody Transaction input){
        Transaction find = transactionRepository.findById(id).get();
        if (find != null){
            find.setId(input.getId());
            find.setReference(input.getReference());
            find.setAccountIban(input.getAccountIban());
            find.setDate(input.getDate());
            find.setAmount(input.getAmount());
            find.setFee(input.getFee());
            find.setDescription(input.getDescription());
            find.setStatus(input.getStatus());
            find.setChannel(input.getChannel());
        }
        Transaction save = transactionRepository.save(find);
        return ResponseEntity.ok(save);
    }

    @PostMapping
    public ResponseEntity<?> post(@RequestBody Transaction input) {
        Transaction save = transactionRepository.save(input);
        return ResponseEntity.ok(save);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable ("id") long id) {
        Optional<Transaction> findById = transactionRepository.findById(id);
        if(findById.get() != null){
            transactionRepository.delete(findById.get());
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/customer/transactions")
    public Transaction getByIban(@RequestParam ("accountIban") String consumerIban){
        return transactionRepository.findByIban(consumerIban);
    }

//    private String getProductName(long id){
//        WebClient build = webClientBuilder
//                .clientConnector(new ReactorClientHttpConnector(client))
//                .baseUrl("http://localhost:8083/product")
//                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                .defaultUriVariables(Collections.singletonMap("url", "http://localhost:8083/product"))
//                .build();
//
//        JsonNode block = build.method(HttpMethod.GET).uri("/" + id)
//                .retrieve().bodyToMono(JsonNode.class).block();
//
//        return block.get("name").asText();
//    }
}
