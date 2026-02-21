package com.ecommerce.userservice.service;

import com.ecommerce.userservice.model.User;
import com.ecommerce.userservice.repository.UserRepository;
import com.ecommerce.userservice.telemetry.TelemetryClient;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private TelemetryClient telemetryClient;
    
    @Value("${jwt.secret:defaultSecretKeyThatIsAtLeast256BitsLongForHS256Algorithm}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:86400000}")
    private int jwtExpirationMs;
    
    public User registerUser(User user) {
        telemetryClient.logEvent("Registering new user: " + user.getUsername(), "INFO");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        telemetryClient.logEvent("User registered successfully with ID: " + savedUser.getId(), "INFO");
        return savedUser;
    }
    
    public String authenticateUser(String username, String password) {
        telemetryClient.logEvent("Authenticating user: " + username, "INFO");
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> {
                telemetryClient.logEvent("Authentication failed: User not found - " + username, "ERROR");
                return new RuntimeException("User not found");
            });
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            telemetryClient.logEvent("Authentication failed: Invalid password for user - " + username, "ERROR");
            throw new RuntimeException("Invalid password");
        }
        
        telemetryClient.logEvent("User authenticated successfully: " + username, "INFO");
        return generateToken(user);
    }
    
    public User getUserById(Long id) {
        telemetryClient.logEvent("Fetching user by ID: " + id, "INFO");
        return userRepository.findById(id)
            .orElseThrow(() -> {
                telemetryClient.logEvent("User not found with ID: " + id, "ERROR");
                return new RuntimeException("User not found");
            });
    }
    
    public List<User> getAllUsers() {
        telemetryClient.logEvent("Fetching all users", "INFO");
        List<User> users = userRepository.findAll();
        telemetryClient.logEvent("Retrieved " + users.size() + " users", "INFO");
        return users;
    }
    
    public User updateUser(Long id, User userDetails) {
        telemetryClient.logEvent("Updating user with ID: " + id, "INFO");
        User user = getUserById(id);
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setEmail(userDetails.getEmail());
        user.setAddress(userDetails.getAddress());
        user.setPhoneNumber(userDetails.getPhoneNumber());
        User updatedUser = userRepository.save(user);
        telemetryClient.logEvent("User updated successfully with ID: " + id, "INFO");
        return updatedUser;
    }
    
    public boolean validateToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                telemetryClient.logEvent("Token validation failed: Empty or null token", "WARN");
                return false;
            }
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            telemetryClient.logEvent("Token validated successfully", "INFO");
            return true;
        } catch (Exception e) {
            telemetryClient.logEvent("Token validation failed: " + e.getMessage(), "WARN");
            return false;
        }
    }
    
    private String generateToken(User user) {
        return Jwts.builder()
            .setSubject(user.getUsername())
            .claim("userId", user.getId())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
    }
    
    private SecretKey getSigningKey() {
        byte[] keyBytes;
        if (jwtSecret.startsWith("base64:")) {
            keyBytes = Base64.getDecoder().decode(jwtSecret.substring(7));
        } else {
            keyBytes = jwtSecret.getBytes();
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}