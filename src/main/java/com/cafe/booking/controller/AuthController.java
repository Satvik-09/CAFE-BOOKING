package com.cafe.booking.controller;

import com.cafe.booking.dto.LoginRequest;
import com.cafe.booking.dto.RegisterRequest;
import com.cafe.booking.entity.UserEntity;
import com.cafe.booking.repository.UserRepository;
import com.cafe.booking.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.cafe.booking.entity.Enum.Role;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request){
        UserEntity user = new UserEntity();
        user.setUsername( request.getUsername());
        user.setPassword(passwordEncoder.encode( request.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);
        return ResponseEntity.ok("Registered Successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request){
        UserEntity user = userRepository.findByUsername(request.getUsername())
                .orElse(null);

          if(user == null || !passwordEncoder.matches(request.getPassword(),user.getPassword())){
              return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials");
          }

         String token = jwtUtil.generateToken(user.getUsername(),user.getRole());
        return ResponseEntity.ok(token);
    }

    @GetMapping("/protected-test")
    public String protectedTest() {
        return "You are authenticated!";
    }
}
