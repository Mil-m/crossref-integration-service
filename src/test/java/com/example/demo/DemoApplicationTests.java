package com.example.demo;

import com.example.demo.db.ArticleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;

@ActiveProfiles("test")
@SpringBootTest
class DemoApplicationTests {

	@MockBean
	ArticleRepository articleRepository;

	@Test
	void contextLoads() {
	}
}
