package com.dkp.exchange.repository;

import com.dkp.exchange.model.User;
import com.dkp.exchange.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_ExistingUser_ShouldReturnUser() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setRole(UserRole.USER);
        user.setUsdtBalance(new BigDecimal("1000"));
        user.setBtcBalance(BigDecimal.ZERO);
        user.setEthBalance(BigDecimal.ZERO);
        entityManager.persist(user);
        entityManager.flush();

        // When
        Optional<User> found = userRepository.findByUsername("testuser");

        // Then
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals(UserRole.USER, found.get().getRole());
    }

    @Test
    void findByUsername_NonExistingUser_ShouldReturnEmpty() {
        // When
        Optional<User> found = userRepository.findByUsername("nonexistent");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void existsByUsername_ExistingUser_ShouldReturnTrue() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setRole(UserRole.USER);
        user.setUsdtBalance(new BigDecimal("1000"));
        user.setBtcBalance(BigDecimal.ZERO);
        user.setEthBalance(BigDecimal.ZERO);
        entityManager.persist(user);
        entityManager.flush();

        // When
        boolean exists = userRepository.existsByUsername("testuser");

        // Then
        assertTrue(exists);
    }

    @Test
    void existsByUsername_NonExistingUser_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Then
        assertFalse(exists);
    }
}
