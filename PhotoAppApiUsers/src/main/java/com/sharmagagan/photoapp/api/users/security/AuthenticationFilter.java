package com.sharmagagan.photoapp.api.users.security;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharmagagan.photoapp.api.users.service.UserService;
import com.sharmagagan.photoapp.api.users.ui.model.CreateUserResponseModel;
import com.sharmagagan.photoapp.api.users.ui.model.LoginRequestModel;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.userdetails.User;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private UserService userService;

	private Environment environment;

	public AuthenticationFilter(UserService userService, Environment environment, AuthenticationManager authManager) {
		super(authManager);
		this.userService = userService;
		this.environment = environment;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
			throws AuthenticationException {
		try {
			LoginRequestModel creds = new ObjectMapper().readValue(req.getInputStream(), LoginRequestModel.class);
			return getAuthenticationManager().authenticate(
					new UsernamePasswordAuthenticationToken(creds.getEmail(), creds.getPassword(), new ArrayList<>()));

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
			Authentication auth) {

		String userName = ((User) auth.getPrincipal()).getUsername();

		String tokenSecret = environment.getProperty("token.secret");

		byte[] secretKeysBytes = Base64.getEncoder().encode(tokenSecret.getBytes());
		SecretKey secretKey = new SecretKeySpec(secretKeysBytes, SignatureAlgorithm.HS512.getJcaName());
		CreateUserResponseModel authenticatedUser = userService.getUserbyEmail(userName);
		Instant now = Instant.now();
		String token = Jwts.builder().subject(authenticatedUser.getEmail())
				.expiration(Date.from(
						now.plusSeconds(Long.parseLong(environment.getProperty("token.expiration.time.seconds")))))
				.issuedAt(Date.from(now)).signWith(secretKey, SignatureAlgorithm.HS512).compact();

		res.addHeader("token", token);
		res.addHeader("userId", authenticatedUser.getUserId());

	}

	@Override
	public void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed)  {
		ObjectMapper objectMapper = new ObjectMapper();
		response.addHeader("timestamp", LocalDateTime.now().toString());
		response.addHeader("exception", failed.getMessage());


		throw new RuntimeException(failed.getMessage(), failed.getCause());

	}

}
