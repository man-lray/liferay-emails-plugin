package com.liferay.email.service;

import com.liferay.email.BaseRestController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
public class EmailService extends BaseRestController {


    public ResponseEntity<String> create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody String json) {

//        log(jwt, _log, json);

        WebClient.Builder builder = WebClient.builder();

        WebClient webClient = builder.baseUrl(
                lxcDXPServerProtocol + "://" + lxcDXPMainDomain
        ).defaultHeader(
                HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE
        ).defaultHeader(
                HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getTokenValue()
        ).defaultHeader(
                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
        ).build();

        JSONObject jsonObject = new JSONObject(json);

        webClient.post()
                .uri("/o/c/emails")
                .bodyValue(
                        json
                )
                .exchangeToMono(
                        clientResponse -> {
                            HttpStatus httpStatus = clientResponse.statusCode();

                            if (httpStatus.is2xxSuccessful()) {
                                return clientResponse.bodyToMono(String.class);
                            } else if (httpStatus.is4xxClientError()) {
                                return Mono.just(httpStatus.getReasonPhrase());
                            }

                            Mono<WebClientResponseException> mono =
                                    clientResponse.createException();

                            return mono.flatMap(Mono::error);
                        }
                ).doOnNext(
                        output -> {
                            if (_log.isInfoEnabled()) {
                                _log.info("Output: " + output);
                            }
                        }
                ).subscribe();

        return new ResponseEntity<>(json, HttpStatus.OK);
    }

    private static final Log _log = LogFactory.getLog(
            EmailService.class);
}
