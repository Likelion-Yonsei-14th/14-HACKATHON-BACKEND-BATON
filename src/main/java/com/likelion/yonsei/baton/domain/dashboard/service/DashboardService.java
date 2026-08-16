package com.likelion.yonsei.baton.domain.dashboard.service;

import com.likelion.yonsei.baton.domain.baton.entity.Baton;
import com.likelion.yonsei.baton.domain.baton.entity.BatonStatus;
import com.likelion.yonsei.baton.domain.baton.repository.BatonRepository;
import com.likelion.yonsei.baton.domain.dashboard.dto.DashboardMetricsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

	public enum Period {
		WEEK,
		MONTH
	}

	private final BatonRepository batonRepository;
	private final Clock clock;

	public DashboardService(BatonRepository batonRepository, Clock clock) {
		this.batonRepository = batonRepository;
		this.clock = clock;
	}

	public DashboardMetricsResponse metrics(Long userId, Period period) {
		LocalDateTime now = LocalDateTime.now(clock);
		LocalDateTime from = period == Period.MONTH ? now.minusMonths(1) : now.minusWeeks(1);

		List<Baton> completedInPeriod = batonRepository.findByUserIdAndStatusAndCompletedAtBetween(
				userId, BatonStatus.COMPLETED, from, now);

		long waitingTimeSavedMinutes = completedInPeriod.stream()
				.filter(b -> b.getActivatedAt() != null && b.getCompletedAt() != null)
				.mapToLong(b -> Duration.between(b.getActivatedAt(), b.getCompletedAt()).toMinutes())
				.sum();

		int activeBatons = (int) batonRepository.countByUserIdAndStatus(userId, BatonStatus.WAITING);
		int needsAttention = (int) batonRepository.countByUserIdAndStatus(userId, BatonStatus.PENDING_REVIEW);

		return new DashboardMetricsResponse(
				waitingTimeSavedMinutes,
				completedInPeriod.size(),
				completedInPeriod.size(),
				activeBatons,
				needsAttention
		);
	}
}
