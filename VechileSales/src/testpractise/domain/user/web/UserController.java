package com.dizzion.portal.domain.user.web;

import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.domain.user.UserService;
import com.dizzion.portal.domain.user.dto.User;
import com.dizzion.portal.domain.user.dto.UserCreateUpdateRequest;
import com.dizzion.portal.security.resource.ResourceWithPermissions;
import com.dizzion.portal.security.resource.UserPermissionsResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Set;

import static com.dizzion.portal.domain.role.Permission.Constants.EDIT_USERS;
import static com.dizzion.portal.domain.role.Permission.Constants.VIEW_USERS;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class UserController {

    private final UserService userService;
    private final UserPermissionsResolver permissionsResolver;

    public UserController(UserService userService, UserPermissionsResolver permissionsResolver) {
        this.userService = userService;
        this.permissionsResolver = permissionsResolver;
    }

    @RequestMapping(path = "/users", method = GET)
    @Secured(VIEW_USERS)
    public Page<ResourceWithPermissions<User>> getPage(Pageable pageRequest, Set<FieldFilter> filters) {
        return userService.getPage(pageRequest, filters).map(permissionsResolver::enrichWithPermissions);
    }

    @RequestMapping(path = "/users/portal-admin", method = GET)
    @Secured(VIEW_USERS)
    public Page<ResourceWithPermissions<User>> getPortalAdminPage(Pageable pageRequest, Set<FieldFilter> filters) {
        return userService.getAdminPortalPage(pageRequest, filters).map(permissionsResolver::enrichWithPermissions);
    }

    @RequestMapping(path = "/users", method = POST)
    @Secured(EDIT_USERS)
    public User create(@RequestBody @Valid UserCreateUpdateRequest user) {
        return userService.create(user);
    }

    @RequestMapping(path = "/users", method = POST, consumes = MULTIPART_FORM_DATA_VALUE)
    @Secured(EDIT_USERS)
    public Set<User> createBatch(@RequestParam("file") MultipartFile file) {
        return userService.createBatch(file);
    }

    @RequestMapping(path = "/users/{id}", method = PUT)
    @Secured(EDIT_USERS)
    public User update(@PathVariable long id, @RequestBody @Valid UserCreateUpdateRequest user) {
        return userService.update(id, user);
    }

    @RequestMapping(path = "/users/{id}", method = DELETE)
    @Secured(EDIT_USERS)
    public void delete(@PathVariable long id) {
        userService.delete(id);
    }

    @RequestMapping(path = "/users/email/uniqueness", method = GET)
    @Secured(EDIT_USERS)
    public boolean checkEmailUnique(String userEmail) {
        return !userService.isEmailTaken(userEmail);
    }

    @RequestMapping(path = "/users/{id}/send-pin", method = PUT)
    @Secured(EDIT_USERS)
    public void sendPin(@PathVariable long id) {
        userService.sendPin(id);
    }
}
