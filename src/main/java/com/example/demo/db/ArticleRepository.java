package com.example.demo.db;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ArticleRepository extends ReactiveCrudRepository<ArticleEntity, String> {}
