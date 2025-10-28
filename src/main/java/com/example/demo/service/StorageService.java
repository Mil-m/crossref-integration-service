package com.example.demo.service;

import com.example.demo.db.ArticleEntity;
import com.example.demo.db.ArticleRepository;
import com.example.demo.model.ArticleInfo;
import com.example.demo.model.PeerReviewInfo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class StorageService {
    private final ArticleRepository repo;

    public StorageService(ArticleRepository repo) { this.repo = repo; }

    public Mono<ArticleInfo> saveAndReturn(ArticleInfo info) {
        if (info.doi() == null || info.doi().isBlank()) {
            return Mono.just(info);
        }

        String authorsStr = (info.authors() == null || info.authors().isEmpty())
                ? null
                : String.join("; ", info.authors());
        Boolean peerBoxed = (info.peerReview() != null) ? info.peerReview().present() : null;

        ArticleEntity e = new ArticleEntity(
                info.doi(), info.title(), authorsStr, info.published(), peerBoxed
        );

        return repo.save(e)
                .map(saved -> {
                    List<String> authors = authorsStr == null ? List.of() : List.of(authorsStr.split("; "));
                    boolean peer = peerBoxed != null && peerBoxed;
                    PeerReviewInfo pr = new PeerReviewInfo(peer, "peer-review", List.of());
                    return new ArticleInfo(saved.getDoi(), saved.getTitle(), authors, saved.getPublished(), pr, null);
                });
    }
}
