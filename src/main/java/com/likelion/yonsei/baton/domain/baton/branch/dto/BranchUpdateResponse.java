package com.likelion.yonsei.baton.domain.baton.branch.dto;

import java.time.LocalDateTime;

public record BranchUpdateResponse(
		Long id,
		LocalDateTime updatedAt
) {
}
