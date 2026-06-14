package org.example.financetrackerapi.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.financetrackerapi.auth.dto.LoginRequest;
import org.example.financetrackerapi.auth.dto.LoginResponse;
import org.example.financetrackerapi.auth.dto.RegisterRequest;
import org.example.financetrackerapi.auth.dto.AuthResponse;
import org.example.financetrackerapi.auth.mapper.UserMapper;
import org.example.financetrackerapi.exception.BadCredentialException;
import org.example.financetrackerapi.exception.EmailAlreadyExistException;
import org.example.financetrackerapi.user.entity.User;
import org.example.financetrackerapi.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService{
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final static Logger log = LoggerFactory.getLogger(AuthService.class);


    /**
     * Registering User
     *
     * @param request is {@link RegisterRequest} object that holds user register request details
     * @return a {@link AuthResponse} object
     * @throws EmailAlreadyExistException if user with given email already exists
     */
    @Transactional
    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting to register User with email: {}", request.getEmail());

        //checking if Username already exists, if exists exception is thrown
        if(repo.existsByEmail(request.getEmail())){
            log.warn("Username already in use. Registering failed.");
            throw new EmailAlreadyExistException("Email already exists");
        }

        //Creating a User object, with request info
        User user = User.create(request.getEmail(), passwordEncoder.encode(request.getPassword()));

        //Persisting Created user object and returning saved user entity
        User savedUser = repo.save(user);
        log.info("registered User successfully");

        //mapping user entity to AuthResponse dto and returning it to controller
        return UserMapper.toAuthResponse(savedUser);
    }


    /**
     * Logging in User
     *
     * @param request is a {@link LoginRequest} object with login request details
     * @return a {@link LoginResponse} object
     * @throws BadCredentialException if given credentials invalid
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        //finding user by email, if not found throws an exception
        User user = repo.findByEmail(request.getEmail()).orElseThrow(()->{
                log.warn("User not found. Failed to log in");
              return new BadCredentialException("Invalid email or Password");
        });

        //checks if password is correct, if not throws an exception
        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            log.warn("Incorrect credentials. Failed to log in");
            throw new BadCredentialException("Invalid email or Password");
        }


        //generates jwt token and stores it in string token
        String token = jwtService.generateToken(user);
        log.info("Successfully logged in");

        //returns dto object to controller
        return new LoginResponse(token,"Bearer");

    }
}
