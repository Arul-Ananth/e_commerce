package org.example.modules.users.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.example.common.dto.PageResponse;
import org.example.modules.auth.dto.AuthResponse;
import org.example.modules.auth.dto.SignupRequest;
import org.example.modules.auth.service.AuthService;
import org.example.modules.users.dto.UserAdminDto;
import org.example.modules.users.dto.request.ToggleEmployeeRoleRequest;
import org.example.modules.users.dto.request.UpdateUserDiscountRequest;
import org.example.modules.users.model.User;
import org.example.modules.users.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public PageResponse<UserAdminDto> getAllUsers(
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return PageResponse.from(userService.getUsers(page, size), item -> item);
    }

    @PatchMapping("/{id}/flag")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserAdminDto> flagUser(@PathVariable("id") Long id,
                                                 @AuthenticationPrincipal User actor) {
        return ResponseEntity.ok(userService.flagUser(id, actor));
    }

    @PatchMapping("/{id}/unflag")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserAdminDto> unflagUser(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.unflagUser(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/discount")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<UserAdminDto> updateUserDiscount(@PathVariable("id") Long id,
                                                           @Valid @RequestBody UpdateUserDiscountRequest request,
                                                           @AuthenticationPrincipal User actor) {
        return ResponseEntity.ok(userService.updateUserDiscount(id, request, actor));
    }

    @PatchMapping("/{id}/employee")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserAdminDto> setEmployeeRole(@PathVariable("id") Long id,
                                                        @Valid @RequestBody ToggleEmployeeRoleRequest request) {
        return ResponseEntity.ok(userService.setEmployeeRole(id, request));
    }

    @PostMapping("/managers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> createManager(@Valid @RequestBody SignupRequest req) {
        return ResponseEntity.ok(authService.registerManager(req));
    }
}
