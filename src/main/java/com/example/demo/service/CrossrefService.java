package com.example.demo.service;

import com.example.demo.model.ArticleInfo;
import com.example.demo.model.PeerReviewInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class CrossrefService {

    private final WebClient client;
    private final StorageService storage;
    private final ObjectMapper mapper = new ObjectMapper();

    public CrossrefService(WebClient crossrefWebClient, StorageService storage) {
        this.client = crossrefWebClient;
        this.storage = storage;
    }

    public Flux<ArticleInfo> fetchArticlesByDois(List<String> dois) {
        int concurrency = 10;
        return Flux.fromIterable(dois)
                .flatMap(this::fetchOne, concurrency)
                .flatMap(storage::saveAndReturn, 4);
    }

    private Mono<ArticleInfo> fetchOne(String doiFromPath) {
        String path = "/works/" + doiFromPath;
        return client.get()
                .uri(path)
                .retrieve()
                .bodyToMono(String.class)
                .map(body -> {
                    try {
                        JsonNode root = mapper.readTree(body);
                        JsonNode msg = root.path("message");

                        String doi = getText(msg, "DOI");
                        if (doi == null || doi.isBlank()) doi = doiFromPath;

                        String title = null;
                        JsonNode titleArr = msg.path("title");
                        if (titleArr.isArray() && titleArr.size() > 0) title = titleArr.get(0).asText(null);

                        List<String> authors = new ArrayList<>();
                        if (msg.has("author") && msg.get("author").isArray()) {
                            for (JsonNode a : msg.get("author")) {
                                String given = a.path("given").asText("");
                                String family = a.path("family").asText("");
                                String full = (given + " " + family).trim();
                                if (!full.isBlank()) authors.add(full);
                            }
                        }

                        String published = extractDate(msg);

                        boolean peer = msg.path("type").asText("").toLowerCase().contains("journal-article");
                        PeerReviewInfo pr = new PeerReviewInfo(peer, "peer-review", List.of());

                        return new ArticleInfo(doi, title, authors, published, pr, null);
                    } catch (Exception e) {
                        return new ArticleInfo(doiFromPath, null, List.of(), null, null, "parse error");
                    }
                });
    }

    private static String getText(JsonNode obj, String field) {
        return obj.hasNonNull(field) ? obj.get(field).asText(null) : null;
    }

    private static String extractDate(JsonNode msg) {
        String d = fromDateParts(msg.path("published-print").path("date-parts"));
        if (d != null) return d;
        d = fromDateParts(msg.path("published-online").path("date-parts"));
        if (d != null) return d;
        d = fromDateParts(msg.path("created").path("date-parts"));
        return d;
    }

    private static String fromDateParts(JsonNode dp) {
        if (dp.isArray() && dp.size() > 0 && dp.get(0).isArray()) {
            JsonNode p = dp.get(0);
            int y = p.size() > 0 ? p.get(0).asInt() : 0;
            Integer m = p.size() > 1 ? p.get(1).asInt() : null;
            Integer d = p.size() > 2 ? p.get(2).asInt() : null;
            if (y > 0) {
                if (m == null) return String.format("%04d", y);
                if (d == null) return String.format("%04d-%02d", y, m);
                return String.format("%04d-%02d-%02d", y, m, d);
            }
        }
        return null;
    }
}
