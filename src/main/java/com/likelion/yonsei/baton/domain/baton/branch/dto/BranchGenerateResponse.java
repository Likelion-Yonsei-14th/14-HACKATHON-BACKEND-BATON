package com.likelion.yonsei.baton.domain.baton.branch.dto;

import java.util.List;

public record BranchGenerateResponse(
		List<BranchSummaryResponse> branches
) {
}
