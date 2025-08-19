package com.ecommerce.userservice.config;

import com.ecommerce.userservice.model.User;
import com.ecommerce.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Only load data if the database is empty
        if (userRepository.count() == 0) {
            loadSeedData();
        }
    }
    
    private void loadSeedData() {
        System.out.println("🌱 Loading User Service seed data...");
        
        // Create sample users with consistent IDs for cross-service integration
        User[] users = {
            createUser(1L, "john_doe", "john.doe@example.com", "password123", 
                      "John", "Doe", "123 Main St, Anytown, ST 12345", "+1-555-0101"),
            createUser(2L, "jane_smith", "jane.smith@example.com", "password123", 
                      "Jane", "Smith", "456 Oak Ave, Springfield, IL 62701", "+1-555-0102"),
            createUser(3L, "bob_wilson", "bob.wilson@example.com", "password123", 
                      "Bob", "Wilson", "789 Pine Rd, Austin, TX 78701", "+1-555-0103"),
            createUser(4L, "alice_johnson", "alice.johnson@example.com", "password123", 
                      "Alice", "Johnson", "321 Elm St, Denver, CO 80201", "+1-555-0104"),
            createUser(5L, "charlie_brown", "charlie.brown@example.com", "password123", 
                      "Charlie", "Brown", "654 Maple Dr, Seattle, WA 98101", "+1-555-0105"),
            createUser(6L, "diana_clark", "diana.clark@example.com", "password123", 
                      "Diana", "Clark", "987 Cedar Ln, Portland, OR 97201", "+1-555-0106"),
            createUser(7L, "frank_miller", "frank.miller@example.com", "password123", 
                      "Frank", "Miller", "147 Birch Ave, Miami, FL 33101", "+1-555-0107"),
            createUser(8L, "grace_lee", "grace.lee@example.com", "password123", 
                      "Grace", "Lee", "258 Willow St, Boston, MA 02101", "+1-555-0108")
        };
        
        for (User user : users) {
            userRepository.save(user);
        }
        
        System.out.println("✅ Created " + users.length + " users");
        System.out.println("🔑 Default password for all users: password123");
    }
    
    private User createUser(Long id, String username, String email, String password, 
                           String firstName, String lastName, String address, String phone) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAddress(address);
        user.setPhoneNumber(phone);
        return user;
    }
}