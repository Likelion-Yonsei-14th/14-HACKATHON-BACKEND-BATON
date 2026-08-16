package com.likelion.yonsei.baton.domain.baton.branch.dto;

import java.util.List;

public record BranchListResponse(
		List<BranchSummaryResponse> branches
) {
}
