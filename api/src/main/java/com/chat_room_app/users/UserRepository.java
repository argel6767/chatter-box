package com.chat_room_app.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAllByUsernameIn(Collection<String> usernames);
    void deleteByUsername(String username);

    /**
     * Find all users whose username is like the given query, except for the user themselves, and if they're verified
     * @param query
     * @param user
     * @return
     */
    @Query("SELECT u FROM User u WHERE LOWER (u.username) LIKE CONCAT('%', :query, '%') " +
            "AND LOWER (u.username) != (:user) AND u.authDetails.isVerified = true")
    List<User> findAllByUsernameLike(@Param("query") String query, @Param("user") String user);
}
