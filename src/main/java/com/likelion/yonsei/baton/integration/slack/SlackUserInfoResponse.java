package com.likelion.yonsei.baton.integration.slack;

public record SlackUserInfoResponse(
		boolean ok,
		String error,
		SlackUser user
) {

	public record SlackUser(
			String id,
			String name,
			SlackUserProfile profile
	) {
	}

	public record SlackUserProfile(
			String displayName,
			String realName
	) {
	}

	/** display_name is often blank for accounts that never set one — fall back to real_name, then the bare user id. */
	public String resolveDisplayName(String fallbackId) {
		if (user == null) {
			return fallbackId;
		}
		if (user.profile() != null && user.profile().displayName() != null && !user.profile().displayName().isBlank()) {
			return user.profile().displayName();
		}
		if (user.profile() != null && user.profile().realName() != null && !user.profile().realName().isBlank()) {
			return user.profile().realName();
		}
		return user.name() != null ? user.name() : fallbackId;
	}
}
