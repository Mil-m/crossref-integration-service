package com.example.demo.service;

import com.example.demo.db.ArticleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CrossrefServiceTest {

    private MockWebServer server;
    private CrossrefService service;

    @BeforeEach
    void setup() throws Exception {
        server = new MockWebServer();
        server.start();

        String baseUrl = server.url("/").toString().replaceAll("/$", "");
        WebClient client = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", "Test/1.0 (+mailto:test@example.com)")
                .build();

        var repo = Mockito.mock(ArticleRepository.class);
        var storage = new StorageService(repo);
        when(repo.save(any())).thenAnswer(inv -> reactor.core.publisher.Mono.just(inv.getArgument(0)));

        service = new CrossrefService(client, storage);
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void parsesTitleAuthorsDateAndPeerReview() throws Exception {
        var mapper = new ObjectMapper();
        var dois = List.of("10.1038/s41586-020-2649-2", "10.1109/5.771073");

        for (String doi : dois) {
            var msg = mapper.createObjectNode();
            msg.put("DOI", doi);
            var titleArr = mapper.createArrayNode();
            titleArr.add("Array programming with NumPy");
            msg.set("title", titleArr);

            var authors = mapper.createArrayNode();
            var a = mapper.createObjectNode();
            a.put("given", "Charles R.");
            a.put("family", "Harris");
            authors.add(a);
            msg.set("author", authors);

            var dpOuter = mapper.createArrayNode();
            var dpInner = mapper.createArrayNode();
            dpInner.add(2020);
            dpInner.add(9);
            dpInner.add(16);
            dpOuter.add(dpInner);
            var publishedPrint = mapper.createObjectNode();
            publishedPrint.set("date-parts", dpOuter);
            msg.set("published-print", publishedPrint);

            msg.put("type", "journal-article");

            var root = mapper.createObjectNode();
            root.set("message", msg);

            server.enqueue(new MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setBody(mapper.writeValueAsString(root)));
        }

        var items = service.fetchArticlesByDois(dois)
                .collectList()
                .block(Duration.ofSeconds(3));
        assertNotNull(items, "Flux returned null list");
        assertEquals(2, items.size(), "Expected 2 items but got: " + items);

        for (int i = 0; i < items.size(); i++) {
            assertNotNull(items.get(i), "Item #" + i + " is null");
            assertNotNull(items.get(i).doi(), "Item #" + i + " has null DOI: " + items.get(i));
        }

        var gotDois = items.stream().map(a -> a.doi()).collect(java.util.stream.Collectors.toSet());
        for (String doi : dois) {
            assertTrue(gotDois.contains(doi), "Missing item for doi=" + doi + ", actual dois=" + gotDois + ", items=" + items);
        }

        var dateRe = Pattern.compile("^\\d{4}(-\\d{2}(-\\d{2})?)?$");

        for (var ainfo : items) {
            assertEquals("Array programming with NumPy", ainfo.title(), "title mismatch for doi=" + ainfo.doi());
            assertNotNull(ainfo.authors(), "authors null for doi=" + ainfo.doi());
            assertTrue(ainfo.authors().contains("Charles R. Harris"), "authors mismatch for doi=" + ainfo.doi() + ": " + ainfo.authors());
            assertNotNull(ainfo.published(), "published null for doi=" + ainfo.doi());
            assertTrue(dateRe.matcher(ainfo.published()).matches(), "published format invalid for doi=" + ainfo.doi() + ": " + ainfo.published());
            assertNotNull(ainfo.peerReview(), "peerReview null for doi=" + ainfo.doi());
            assertTrue(ainfo.peerReview().present(), "peerReview.present should be true for doi=" + ainfo.doi());
            assertNull(ainfo.error(), "error should be null for doi=" + ainfo.doi() + " but was: " + ainfo.error());
        }
    }

}
