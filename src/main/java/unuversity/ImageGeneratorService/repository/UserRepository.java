package unuversity.ImageGeneratorService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unuversity.ImageGeneratorService.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
