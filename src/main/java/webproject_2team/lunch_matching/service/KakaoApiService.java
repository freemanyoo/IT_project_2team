package webproject_2team.lunch_matching.service;

import  webproject_2team.lunch_matching.dto.KakaoLocalSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;

@Service
@Log4j2
@RequiredArgsConstructor
public class KakaoApiService {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @Value("${kakao.local.api.keyword.url}")
    private String kakaoLocalApiKeywordUrl;

    private final RestTemplate restTemplate;

    public KakaoLocalSearchResponse searchPlacesByKeyword(String keyword, int page) {
        log.info("카카오 API 키워드 장소 검색을 시작합니다. 검색어: {}, 페이지: {}", keyword, page);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        URI targetUrl = UriComponentsBuilder
                .fromUriString(kakaoLocalApiKeywordUrl)
                .queryParam("query", keyword)
                .queryParam("size", 15)
                .queryParam("page", page)
                .build()
                .encode()
                .toUri();

        log.info("요청할 URI: " + targetUrl);

        try {
            ResponseEntity<KakaoLocalSearchResponse> response = restTemplate.exchange(
                    targetUrl,
                    HttpMethod.GET,
                    entity,
                    KakaoLocalSearchResponse.class
            );

            log.info("카카오 API 호출 성공. 응답 코드: " + response.getStatusCode());
            return response.getBody();

        } catch (Exception e) {
            log.error("카카오 API 호출 중 오류가 발생했습니다.", e);
            return KakaoLocalSearchResponse.builder()
                    .documents(Collections.emptyList())
                    .meta(new KakaoLocalSearchResponse.Meta())
                    .build();
        }
    }
}