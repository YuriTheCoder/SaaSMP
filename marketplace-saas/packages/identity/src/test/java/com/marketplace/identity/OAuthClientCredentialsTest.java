package com.marketplace.identity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OAuthClientCredentialsTest {
    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Test
    void token_via_client_credentials() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String basic = Base64.getEncoder().encodeToString("test-client:test-secret".getBytes(StandardCharsets.UTF_8));
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + basic);

        HttpEntity<String> req = new HttpEntity<>("grant_type=client_credentials&scope=payments:write", headers);
        ResponseEntity<Map> res = rest.postForEntity("http://localhost:" + port + "/oauth2/token", req, Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).containsKeys("access_token", "token_type");
    }
}


