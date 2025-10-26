package com.example.demo.model;

import java.util.List;

public record ArticleInfo(
        String doi,
        String title,
        List<String> authors,
        String published,
        PeerReviewInfo peerReview,
        String error
) {}
