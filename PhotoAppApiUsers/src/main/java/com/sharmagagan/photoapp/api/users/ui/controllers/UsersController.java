package com.sharmagagan.photoapp.api.users.ui.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sharmagagan.photoapp.api.users.service.UserService;
import com.sharmagagan.photoapp.api.users.ui.model.CreateUserRequestModel;
import com.sharmagagan.photoapp.api.users.ui.model.CreateUserResponseModel;
import com.sharmagagan.photoapp.api.users.ui.model.UserResponseModel;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UsersController {

	@Autowired
	private Environment envi;

	@Autowired
	private UserService userService;

	@GetMapping("/status/check")
	public String getStatus() {
		return "Working on port number " + envi.getProperty("local.server.port") + ", With token " +envi.getProperty("token.secret");
	}

	@GetMapping(value = "/{userId}")
	public ResponseEntity<UserResponseModel> getUsers(@PathVariable String userId) {
		UserResponseModel createdUser = userService.getUserbyUserId(userId);
		return ResponseEntity.status(HttpStatus.OK).body(createdUser);
	}

	@PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }, produces = {
			MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public ResponseEntity<CreateUserResponseModel> createUser(@Valid @RequestBody CreateUserRequestModel userDetails) {

		CreateUserResponseModel createdUser = userService.createUser(userDetails);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);

	}

}
