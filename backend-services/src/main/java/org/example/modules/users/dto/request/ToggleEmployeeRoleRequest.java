package org.example.modules.users.dto.request;

import jakarta.validation.constraints.NotNull;

public record ToggleEmployeeRoleRequest(
        @NotNull Boolean enabled
) {
}
