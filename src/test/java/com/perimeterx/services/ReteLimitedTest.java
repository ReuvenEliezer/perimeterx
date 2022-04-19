package com.perimeterx.services;

import com.perimeterx.PerimeterXApp;
import com.perimeterx.entities.RateLimitedResponse;
import com.perimeterx.entities.UrlRequest;
import com.perimeterx.utils.WsAddressConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        classes = PerimeterXApp.class,
        args = {"5000", "3"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ReteLimitedTest {

    @Value("${server.port}")
    private String port;
    private static final String localhost = "http://localhost:";
    public static String perimeterXFullUrl;

    @BeforeEach
    public void setUp() {
        perimeterXFullUrl = localhost + port + WsAddressConstants.perimeterXLogicUrl;
    }

    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void reteLimitedTest() throws InterruptedException {
        assertFalse(restTemplate.postForObject(perimeterXFullUrl, new UrlRequest("abc"), RateLimitedResponse.class).isBlock());
        assertFalse(restTemplate.postForObject(perimeterXFullUrl, new UrlRequest("abc"), RateLimitedResponse.class).isBlock());
        assertFalse(restTemplate.postForObject(perimeterXFullUrl, new UrlRequest("abc"), RateLimitedResponse.class).isBlock());
        assertTrue(restTemplate.postForObject(perimeterXFullUrl, new UrlRequest("abc"), RateLimitedResponse.class).isBlock());
        Thread.sleep(5000);
        assertFalse(restTemplate.postForObject(perimeterXFullUrl, new UrlRequest("abc"), RateLimitedResponse.class).isBlock());
        assertFalse(restTemplate.postForObject(perimeterXFullUrl, new UrlRequest("foo"), RateLimitedResponse.class).isBlock());
        assertFalse(restTemplate.postForObject(perimeterXFullUrl, new UrlRequest("foo"), RateLimitedResponse.class).isBlock());
        assertFalse(restTemplate.postForObject(perimeterXFullUrl, new UrlRequest("foo"), RateLimitedResponse.class).isBlock());
        assertTrue(restTemplate.postForObject(perimeterXFullUrl, new UrlRequest("foo"), RateLimitedResponse.class).isBlock());
    }


    @Test
    public void reteLimitedTest2() throws InterruptedException {
        String s = UUID.randomUUID().toString();
        ;
        for (int i = 0; i < 3; i++) {
            Thread.sleep(1000);
            assertFalse(restTemplate.postForObject(perimeterXFullUrl, new UrlRequest(s), RateLimitedResponse.class).isBlock());
        }

        Thread.sleep(15000);
        IntStream.range(0, 10).forEach(e ->
                assertFalse(restTemplate.postForObject(perimeterXFullUrl, new UrlRequest(UUID.randomUUID().toString()), RateLimitedResponse.class).isBlock()));
        Thread.sleep(5000);

    }

}
