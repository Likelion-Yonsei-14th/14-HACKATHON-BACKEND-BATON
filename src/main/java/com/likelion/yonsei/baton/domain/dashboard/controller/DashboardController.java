package com.likelion.yonsei.baton.domain.dashboard.controller;

import com.likelion.yonsei.baton.common.response.ApiResponse;
import com.likelion.yonsei.baton.common.web.CurrentUserId;
import com.likelion.yonsei.baton.domain.dashboard.dto.DashboardMetricsResponse;
import com.likelion.yonsei.baton.domain.dashboard.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	@GetMapping("/metrics")
	public ApiResponse<DashboardMetricsResponse> metrics(
			@CurrentUserId Long userId,
			@RequestParam(defaultValue = "WEEK") DashboardService.Period period
	) {
		return ApiResponse.success(dashboardService.metrics(userId, period));
	}
}
