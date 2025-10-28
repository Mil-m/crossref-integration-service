package com.example.demo.controller;

import com.example.demo.controller.ArticleController;
import com.example.demo.model.ArticleInfo;
import com.example.demo.model.ArticleRequest;
import com.example.demo.model.PeerReviewInfo;
import com.example.demo.service.CrossrefService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;

@WebFluxTest(controllers = ArticleController.class)
class ArticleControllerTest {

    @Autowired
    WebTestClient client;

    @MockBean
    CrossrefService service;

    @Test
    void postReturnsFluxOfArticles() {
        var req = new ArticleRequest(List.of("10.1000/xyz", "10.2000/abc"));
        var a1  = new ArticleInfo("10.1000/xyz","Title1", List.of("Doe, John"),"2020-01-01",
                new PeerReviewInfo(false, null, List.of()), null);
        var a2  = new ArticleInfo("10.2000/abc","Title2", List.of("Roe, Jane"),"2021",
                new PeerReviewInfo(true, "peer-review", List.of("has-review")), null);

        Mockito.when(service.fetchArticlesByDois(req.dois()))
                .thenReturn(Flux.just(a1, a2));

        client.post().uri("/get-article-info-by-doi")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].doi").isEqualTo("10.1000/xyz")
                .jsonPath("$[1].peerReview.present").isEqualTo(true);
    }
}
