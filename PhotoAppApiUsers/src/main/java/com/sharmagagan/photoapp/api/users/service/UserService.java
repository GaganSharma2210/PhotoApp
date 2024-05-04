package com.sharmagagan.photoapp.api.users.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.sharmagagan.photoapp.api.users.ui.model.CreateUserRequestModel;
import com.sharmagagan.photoapp.api.users.ui.model.CreateUserResponseModel;
import com.sharmagagan.photoapp.api.users.ui.model.UserResponseModel;

public interface UserService extends UserDetailsService {

	public CreateUserResponseModel createUser(CreateUserRequestModel createUserRequestModel);
	
	public CreateUserResponseModel getUserbyEmail(String email);
	
	public UserResponseModel getUserbyUserId(String userId);
	

}
