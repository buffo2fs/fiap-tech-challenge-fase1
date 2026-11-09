package br.com.fiap.restaurant_management.repository;

import br.com.fiap.restaurant_management.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByLogin(String login);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByLoginAndIdNot(String login, Long id);

    Optional<User> findByLogin(String login);

    Page<User> findByNameContainingIgnoreCase(String name, Pageable pageable);

}
