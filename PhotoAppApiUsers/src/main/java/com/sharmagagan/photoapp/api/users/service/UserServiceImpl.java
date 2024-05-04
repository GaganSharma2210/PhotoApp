package com.sharmagagan.photoapp.api.users.service;

import java.util.*;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.sharmagagan.photoapp.api.users.data.AlbumServiceClient;
import com.sharmagagan.photoapp.api.users.data.UserEntity;
import com.sharmagagan.photoapp.api.users.ui.model.AlbumResponseModel;
import com.sharmagagan.photoapp.api.users.ui.model.CreateUserRequestModel;
import com.sharmagagan.photoapp.api.users.ui.model.CreateUserResponseModel;
import com.sharmagagan.photoapp.api.users.ui.model.UserResponseModel;

import feign.FeignException;

import com.sharmagagan.photoapp.api.users.data.UsersRepository;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UsersRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private AlbumServiceClient albumClient;
	
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public CreateUserResponseModel createUser(CreateUserRequestModel createUserRequestModel) {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		UserEntity userEntity = modelMapper.map(createUserRequestModel, UserEntity.class);
		userEntity.setUserId(UUID.randomUUID().toString());
		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(createUserRequestModel.getPassword()));
		userRepository.save(userEntity);
		CreateUserResponseModel createdUser = modelMapper.map(userEntity, CreateUserResponseModel.class);
		return createdUser;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserEntity userEntity = userRepository.findByEmail(username);
		if (userEntity == null)
			throw new UsernameNotFoundException(username);
		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), true, true, true, true,
				new ArrayList<>());
	}

	@Override
	public CreateUserResponseModel getUserbyEmail(String email) {
		UserEntity userEntity = userRepository.findByEmail(email);
		if (userEntity == null)
			throw new UsernameNotFoundException(email);
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		CreateUserResponseModel createdUser = modelMapper.map(userEntity, CreateUserResponseModel.class);
		return createdUser;
	}

	@Override
	public UserResponseModel getUserbyUserId(String userId) {
		UserEntity userEntity = userRepository.findByUserId(userId);
		if (userEntity == null)
			throw new UsernameNotFoundException(userId);
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		UserResponseModel createdUser = modelMapper.map(userEntity, UserResponseModel.class);
		String albumUrl = String.format(environment.getProperty("albums.url"), userId);
		
		/*ParameterizedTypeReference<List<AlbumResponseModel>> parameterizedTypeReference = new ParameterizedTypeReference<List<AlbumResponseModel>>() {};
		ResponseEntity<List<AlbumResponseModel>> albumListResponse = restTemplate.exchange(albumUrl, HttpMethod.GET, null,
				parameterizedTypeReference);
		
		List<AlbumResponseModel> albumList = albumListResponse.getBody();
		*/
		List<AlbumResponseModel> albumList = null;
				
				try {
					albumList = albumClient.getAlbums(userId);
				} catch (FeignException e) {
					logger.error(e.getLocalizedMessage());
				}
		
		createdUser.setAlbumResponse(albumList);
		
		return createdUser;
	}

}
