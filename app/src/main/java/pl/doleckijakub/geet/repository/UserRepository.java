package pl.doleckijakub.geet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.doleckijakub.geet.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}