package com.example.demo.controller;

import com.example.demo.model.ArticleInfo;
import com.example.demo.model.ArticleRequest;
import com.example.demo.service.CrossrefService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
public class ArticleController {

    private final CrossrefService service;

    public ArticleController(CrossrefService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String root() {
        return "Crossref DOI Lookup API is running. POST /get-article-info-by-doi";
    }

    @PostMapping(value = "/get-article-info-by-doi",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ArticleInfo> getArticleInfo(@Valid @RequestBody ArticleRequest req) {
        return service.fetchArticlesByDois(req.dois());
    }
}
