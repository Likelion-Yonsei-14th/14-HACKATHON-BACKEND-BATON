package com.likelion.yonsei.baton.domain.dashboard.dto;

public record DashboardMetricsResponse(
		long waitingTimeSavedMinutes,
		int roundTripsSkipped,
		int completedWhileOffline,
		int activeBatons,
		int needsAttention
) {
}
