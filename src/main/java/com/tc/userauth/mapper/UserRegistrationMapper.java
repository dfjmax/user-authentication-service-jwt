package com.tc.userauth.mapper;

import com.tc.userauth.dto.RegistrationRequestDto;
import com.tc.userauth.dto.RegistrationResponseDto;
import com.tc.userauth.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserRegistrationMapper {

    public User toEntity(RegistrationRequestDto registrationRequestDto) {
        final var user = new User();

        user.setEmail(registrationRequestDto.email());
        user.setUsername(registrationRequestDto.username());
        user.setPassword(registrationRequestDto.password());

        return user;
    }

    public RegistrationResponseDto toResponseDto(final User user) {
        return new RegistrationResponseDto(user.getEmail(), user.getUsername());
    }

}