package com.likelion.yonsei.baton.domain.baton.service;

import com.likelion.yonsei.baton.domain.baton.entity.Baton;
import com.likelion.yonsei.baton.domain.baton.entity.BatonStatus;
import com.likelion.yonsei.baton.domain.baton.repository.BatonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * "설정한 시간 안에 상대 답장이 없으면 바통이 만료됩니다" (개인설정 화면 문구) — nothing else in
 * the app moved a WAITING baton to EXPIRED once its expires_at passed, so it just sat there
 * forever showing "대기 중" no matter how much time went by. This is the missing timeout side of
 * that promise; the counterpart-replied side is {@link com.likelion.yonsei.baton.integration.slack.SlackPollingService}.
 */
@Service
public class BatonExpiryService {

	private static final Logger log = LoggerFactory.getLogger(BatonExpiryService.class);

	private final BatonRepository batonRepository;
	private final Clock clock;

	public BatonExpiryService(BatonRepository batonRepository, Clock clock) {
		this.batonRepository = batonRepository;
		this.clock = clock;
	}

	@Scheduled(fixedDelay = 60_000, initialDelay = 20_000)
	@Transactional
	public void expireOverdueBatons() {
		LocalDateTime now = LocalDateTime.now(clock);
		List<Baton> overdue = batonRepository.findByStatusAndExpiresAtBefore(BatonStatus.WAITING, now);
		for (Baton baton : overdue) {
			try {
				baton.expire();
			} catch (Exception e) {
				log.warn("Failed to expire baton={}", baton.getId(), e);
			}
		}
		if (!overdue.isEmpty()) {
			log.info("Expired {} overdue baton(s)", overdue.size());
		}
	}
}
