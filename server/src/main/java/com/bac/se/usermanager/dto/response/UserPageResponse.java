package com.bac.se.usermanager.dto.response;

import java.util.List;

public record UserPageResponse(List<UserResponse> userResponses,
                               Integer pageNumber,
                               Integer pageSize,
                               long totalElements,
                               int totalPages,
                               boolean isLast
                               ) {
}
