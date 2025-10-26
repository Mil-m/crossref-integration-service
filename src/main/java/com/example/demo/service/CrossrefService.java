package com.example.demo.service;

import com.example.demo.model.ArticleInfo;
import com.example.demo.model.PeerReviewInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class CrossrefService {

    private final WebClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    public CrossrefService(WebClient crossrefWebClient) {
        this.client = crossrefWebClient;
    }

    public Flux<ArticleInfo> fetchArticlesByDois(List<String> dois) {
        int concurrency = 10;
        return Flux.fromIterable(dois)
                .flatMap(this::fetchOne, concurrency);
    }

    private Mono<ArticleInfo> fetchOne(String doiRaw) {
        String doi = doiRaw == null ? "" : doiRaw.trim();
        if (doi.isEmpty()) {
            return Mono.just(new ArticleInfo(doiRaw, null, null, null,
                    new PeerReviewInfo(false, null, List.of()), "Empty DOI"));
        }

        String encoded = UriUtils.encodePath(doi, StandardCharsets.UTF_8);
        String path = "/works/" + encoded;

        return client.get()
                .uri(path)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), resp ->
                        resp.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(b -> Mono.error(new RuntimeException(
                                        "Client error " + resp.statusCode().value()))))
                .onStatus(status -> status.is5xxServerError(), resp ->
                        resp.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(b -> Mono.error(new RuntimeException(
                                        "Server error " + resp.statusCode().value()))))
                .bodyToMono(JsonNode.class)
                .map(root -> parseOne(doi, root))
                .onErrorResume(ex ->
                        Mono.just(new ArticleInfo(doi, null, null, null,
                                new PeerReviewInfo(false, null, List.of()),
                                "Failed to fetch: " + ex.getMessage()))
                );
    }

    private ArticleInfo parseOne(String doi, JsonNode root) {
        JsonNode msg = root.path("message");

        // title
        String title = null;
        JsonNode titles = msg.path("title");
        if (titles.isArray() && titles.size() > 0) title = safeText(titles.get(0));

        // authors
        List<String> authors = new ArrayList<>();
        JsonNode authArr = msg.path("author");
        if (authArr.isArray()) {
            for (JsonNode a : authArr) {
                String family = safeText(a.path("family"));
                String given  = safeText(a.path("given"));
                String name   = safeText(a.path("name"));
                if (!family.isBlank() || !given.isBlank()) {
                    String formatted = (family + ", " + given).trim().replaceAll(",\\s*$", "");
                    authors.add(formatted);
                } else if (!name.isBlank()) {
                    authors.add(name);
                }
            }
        }

        // publish date
        String published = extractDate(msg, "issued");
        if (published == null) published = extractDate(msg, "published-print");
        if (published == null) published = extractDate(msg, "published-online");

        // peer review
        boolean prPresent = false;
        String prType = null;
        List<String> relationHints = new ArrayList<>();

        String type = safeText(msg.path("type"));
        if ("peer-review".equalsIgnoreCase(type)) {
            prPresent = true;
            prType = "peer-review";
        }
        JsonNode rel = msg.path("relation");
        if (rel.isObject()) {
            Iterator<String> it = rel.fieldNames();
            while (it.hasNext()) {
                String key = it.next();
                if (key.toLowerCase().contains("review")) {
                    prPresent = true;
                    relationHints.add(key);
                }
            }
        }

        return new ArticleInfo(doi, title, authors, published,
                new PeerReviewInfo(prPresent, prType, relationHints), null);
    }

    private String extractDate(JsonNode msg, String field) {
        JsonNode dp = msg.path(field).path("date-parts");
        if (dp.isArray() && dp.size() > 0 && dp.get(0).isArray()) {
            JsonNode parts = dp.get(0);
            String y = parts.size() > 0 ? safeText(parts.get(0)) : null;
            String m = parts.size() > 1 ? pad2(safeText(parts.get(1))) : null;
            String d = parts.size() > 2 ? pad2(safeText(parts.get(2))) : null;
            if (y != null && !y.isBlank()) {
                if (m == null) return y;
                if (d == null) return y + "-" + m;
                return y + "-" + m + "-" + d;
            }
        }
        return null;
    }

    private String pad2(String s) {
        if (s == null || s.isBlank()) return null;
        return s.length() == 1 ? "0" + s : s;
    }

    private String safeText(JsonNode n) {
        return n == null || n.isMissingNode() || n.isNull() ? "" : n.asText("");
    }
}
