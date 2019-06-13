package com.dizzion.portal.security;

import com.dizzion.portal.domain.user.dto.User;
import com.dizzion.portal.domain.user.persistence.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminProvider {
    private static final long ADMIN_USER_ID = 1L;

    private UserRepository userRepo;

    public AdminProvider(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Transactional(readOnly = true)
    public User getAdmin() {
        return User.from(userRepo.findOne(ADMIN_USER_ID));
    }
}
