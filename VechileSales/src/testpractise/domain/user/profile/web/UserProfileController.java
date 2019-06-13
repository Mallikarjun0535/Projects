package com.dizzion.portal.domain.user.profile.web;

import com.dizzion.portal.domain.user.profile.UserProfileService;
import com.dizzion.portal.domain.user.profile.dto.UserProfile;
import com.dizzion.portal.domain.user.profile.dto.UserProfileUpdateRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @RequestMapping(path = "/user-profiles/{id}", method = GET)
    public UserProfile getProfile(@PathVariable long id) {
        return userProfileService.getProfile(id);
    }

    @RequestMapping(path = "/user-profiles/{id}", method = PUT)
    public void updateProfile(@PathVariable long id, @RequestBody @Valid UserProfileUpdateRequest user) {
        userProfileService.updateProfile(id, user);
    }
}
