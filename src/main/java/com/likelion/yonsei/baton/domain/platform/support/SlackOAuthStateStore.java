package com.likelion.yonsei.baton.domain.platform.support;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory CSRF state for the Slack OAuth redirect round-trip.
 * Single-instance only; move to a shared store (Redis) before scaling out to multiple app instances.
 */
@Component
public class SlackOAuthStateStore {

	private static final long TTL_SECONDS = 60 * 10;

	private record Entry(Long userId, Instant expiresAt) {
	}

	private final Map<String, Entry> states = new ConcurrentHashMap<>();
	private final Clock clock;

	public SlackOAuthStateStore(Clock clock) {
		this.clock = clock;
	}

	public String issue(Long userId) {
		String state = UUID.randomUUID().toString();
		states.put(state, new Entry(userId, clock.instant().plusSeconds(TTL_SECONDS)));
		return state;
	}

	public Long consume(String state) {
		Entry entry = states.remove(state);
		if (entry == null || entry.expiresAt().isBefore(clock.instant())) {
			return null;
		}
		return entry.userId();
	}
}
