package org.example.financetrackerapi.auth;

import lombok.AllArgsConstructor;
import org.example.financetrackerapi.exception.BadCredentialException;
import org.example.financetrackerapi.exception.EmailAlreadyExistException;
import org.example.financetrackerapi.user.User;
import org.example.financetrackerapi.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    static String dummyHash = "$2a$10$7sF3G5rPzQJv0nKx6Y8Q9e1uD9zT2oH5Lw7Xc8Vb1NmKpQrStUvWx";


    public AuthResponse register(RegisterRequest request) {

        if(repo.existsByEmail(request.getEmail())){
            throw new EmailAlreadyExistException("Email already exists");
        }

        User user = User.create(request.getEmail(), passwordEncoder.encode(request.getPassword()));

        repo.save(user);

        return new AuthResponse(request.getEmail(), "Successfully registered");
    }

    public LoginResponse login(LoginRequest request) {

        User user = repo.findByEmail(request.getEmail()).orElse(null);

        String passwordCheck;

       if(user != null){
           passwordCheck = user.getPassword();
       }
       else{
           passwordCheck = dummyHash;
       }

       boolean matches = passwordEncoder.matches(request.getPassword(), passwordCheck);

       if(user == null || !matches){
           throw new org.springframework.security.authentication.BadCredentialsException("Invalid Username or Password");
       }

        String token = jwtService.generateToken(user);

        return new LoginResponse(token,"Bearer");

    }
}
