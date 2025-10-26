package com.example.demo.model;

import java.util.List;

public record PeerReviewInfo(
        boolean present,
        String type,
        List<String> relationHints
) {}
