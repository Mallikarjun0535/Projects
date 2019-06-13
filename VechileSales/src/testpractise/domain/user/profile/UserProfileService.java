package com.dizzion.portal.domain.user.profile;

import com.dizzion.portal.domain.common.AbstractCrudService;
import com.dizzion.portal.domain.common.ScopedEntityService;
import com.dizzion.portal.domain.exception.LastUserWithEnabledNotificationsException;
import com.dizzion.portal.domain.user.dto.User;
import com.dizzion.portal.domain.user.persistence.UserRepository;
import com.dizzion.portal.domain.user.persistence.entity.UserEntity;
import com.dizzion.portal.domain.user.profile.dto.UserProfile;
import com.dizzion.portal.domain.user.profile.dto.UserProfileUpdateRequest;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@Transactional
public class UserProfileService extends AbstractCrudService<UserEntity> {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticatedUserAccessor auth;

    public UserProfileService(UserRepository userRepo,
                              PasswordEncoder passwordEncoder,
                              AuthenticatedUserAccessor auth,
                              ScopedEntityService scopedEntityService) {
        super(scopedEntityService);
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.auth = auth;
    }

    @Transactional(readOnly = true)
    public UserProfile getProfile(long id) {
        throwExceptionIfIdIsNotOfLoggedUser(id);
        return UserProfile.from(User.from(userRepo.findOne(id)));
    }

    public void updateProfile(long id, UserProfileUpdateRequest userProfile) {
        throwExceptionIfIdIsNotOfLoggedUser(id);

        UserEntity existing = userRepo.findOne(id);
        if (userProfile.getNotificationMethods().isEmpty()) {
            throwExceptionIfLastUserWithEnabledNotifications(existing);
        }

        if (existing.getOrganization().isTwoFactorAuth()
                && (!userProfile.getMobilePhoneNumber().isPresent() || isBlank(userProfile.getMobilePhoneNumber().get()))) {
            throw new IllegalArgumentException("Cannot remove phone number in the org with two-factor auth enabled");
        }

        existing.setEmail(userProfile.getEmail());
        existing.setNotificationMethods(userProfile.getNotificationMethods());
        existing.setFirstName(userProfile.getFirstName());
        existing.setLastName(userProfile.getLastName());
        existing.setPin(userProfile.getPin());
        userProfile.getMobilePhoneNumber().ifPresent(existing::setMobilePhoneNumber);
        userProfile.getWorkPhoneNumber().ifPresent(existing::setWorkPhoneNumber);

        if (userProfile.getOldPassword().isPresent() || userProfile.getNewPassword().isPresent()) {
            throwExceptionIfPasswordFieldsIncorrect(userProfile, existing);
            existing.setPassword(passwordEncoder.encode(userProfile.getNewPassword().get()));
        }

        save(existing);
    }

    private void throwExceptionIfIdIsNotOfLoggedUser(long id) {
        if (id != auth.getAuthenticatedUser().getUser().getId()) {
            throw new IllegalArgumentException();
        }
    }

    private void throwExceptionIfPasswordFieldsIncorrect(UserProfileUpdateRequest user, UserEntity existing) {
        if (!areAllPasswordFieldsPresented(user)
                || !areOldPasswordsEquals(existing.getPassword(), user.getOldPassword().get())) {
            throw new IllegalArgumentException();
        }
    }

    private void throwExceptionIfLastUserWithEnabledNotifications(UserEntity user) {
        if (userRepo.countUsersWithEnabledNotifications(user.getOrganization().getId()) <= 1) {
            throw new LastUserWithEnabledNotificationsException();
        }
    }

    private boolean areAllPasswordFieldsPresented(UserProfileUpdateRequest user) {
        return user.getOldPassword().isPresent() && user.getNewPassword().isPresent();
    }

    private boolean areOldPasswordsEquals(String oldPasswordFromDb, String oldPasswordFromRequest) {
        return passwordEncoder.matches(oldPasswordFromRequest, oldPasswordFromDb);
    }
}
