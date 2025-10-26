package com.example.demo.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ArticleRequest(
        @NotNull
        @Size(min = 1, max = 200, message = "Provide 1..200 DOIs")
        List<String> dois
) {}
