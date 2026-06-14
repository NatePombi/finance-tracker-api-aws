package org.example.financetrackerapi.auth.mapper;

import org.example.financetrackerapi.auth.dto.AuthResponse;
import org.example.financetrackerapi.user.entity.User;

public class UserMapper {


    public static AuthResponse toAuthResponse(User user) {

        if (user == null) {return null;}

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                "Successfully Registered"
        );
    }
}
