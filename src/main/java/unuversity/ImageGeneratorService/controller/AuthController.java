package unuversity.ImageGeneratorService.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unuversity.ImageGeneratorService.model.User;
import unuversity.ImageGeneratorService.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody User user) {
        logger.info("Received registration request for user: {}", user.getUsername());
        Map<String, String> response = new HashMap<>();
        if (userService.findByUsername(user.getUsername().toLowerCase()) != null) {
            response.put("message", "User already exists");
            return ResponseEntity.badRequest().body(response);
        }
        user.setUsername(user.getUsername().toLowerCase());
        userService.saveUser(user);
        response.put("message", "User registered successfully");
        return ResponseEntity.ok(response);
    }


    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody User user) {
        logger.info("Received login request for user: {}", user.getUsername());
        Map<String, String> response = new HashMap<>();
        User foundUser = userService.findByUsername(user.getUsername().toLowerCase());
        if (foundUser != null && foundUser.getPassword().equals(user.getPassword())) {
            response.put("message", "Login successful");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Invalid username or password");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
