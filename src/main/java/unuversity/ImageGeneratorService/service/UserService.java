package unuversity.ImageGeneratorService.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import unuversity.ImageGeneratorService.model.User;
import unuversity.ImageGeneratorService.repository.UserRepository;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveUser(User user) {
        logger.info("Saving user: {}", user.getUsername());
        user.setUsername(user.getUsername().toLowerCase());
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        logger.info("Finding user by username: {}", username.toLowerCase());
        return userRepository.findByUsername(username.toLowerCase()).orElse(null);
    }
}