package com.ecom.pradeep.angadi_bk.repo;

import com.ecom.pradeep.angadi_bk.model.RefreshToken;
import com.ecom.pradeep.angadi_bk.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}
