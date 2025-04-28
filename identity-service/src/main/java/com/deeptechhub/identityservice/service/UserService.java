package com.deeptechhub.identityservice.service;

import com.deeptechhub.common.dto.UserDto;
import com.deeptechhub.identityservice.dto.UpdateProfileDTO;
import com.deeptechhub.identityservice.dto.UserProfileDTO;
import com.deeptechhub.identityservice.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    public UserDto findByUsername(String username);
    public UserDto findById(Long id);
    public UserProfileDTO getCurrentUserProfile(String username);
    public UserProfileDTO updateUserProfile(String username, UpdateProfileDTO updateProfileDTO);
    public List<UserProfileDTO> listAllUsers();
}
