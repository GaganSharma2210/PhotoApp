package com.sharmagagan.photoapp.api.gateway;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.*;
import reactor.core.publisher.Mono;

@Component
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

	@Autowired
	private Environment environment;

	public AuthorizationHeaderFilter() {
		super(Config.class);
	}

	public static class Config {
		// put config prop here
	}

	@Override
	public GatewayFilter apply(Config config) {

		return (exchange, chain) -> {

			ServerHttpRequest request = exchange.getRequest();
			if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
				return onError(exchange, "No Authorization Header Found ", HttpStatus.UNAUTHORIZED);
			}
			String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
			String jwt = authorizationHeader.replace("Bearer ", "");
			if (!isValid(jwt)) {
				return onError(exchange, "Jwt token is not valid ", HttpStatus.UNAUTHORIZED);
			}

			return chain.filter(exchange);
		};
	}

	private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(httpStatus);

		return response.setComplete();

	}

	private boolean isValid(String jwt) {
		boolean isValid = true;
		String subject = null;

		String tokenSecret = environment.getProperty("token.secret");
		byte[] secretKeysBytes = Base64.getEncoder().encode(tokenSecret.getBytes());
		SecretKey secretKey = new SecretKeySpec(secretKeysBytes, SignatureAlgorithm.HS512.getJcaName());

		JwtParser jwtParser = Jwts.parser().verifyWith(secretKey).build();

		try {
			Jwt<Header, Claims> parsedToken = (Jwt<Header, Claims>) jwtParser.parse(jwt);
			subject = parsedToken.getBody().getSubject();
		} catch (Exception ex) {
			isValid = false;
		}
		if (subject == null || subject.isEmpty()) {
			isValid = false;
		}

		return isValid;

	}

}
