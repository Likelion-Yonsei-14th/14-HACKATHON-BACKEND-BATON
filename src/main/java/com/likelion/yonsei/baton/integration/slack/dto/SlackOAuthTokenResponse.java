package com.likelion.yonsei.baton.integration.slack.dto;

public record SlackOAuthTokenResponse(
		boolean ok,
		String error,
		String accessToken,
		String refreshToken,
		Long expiresIn,
		String scope,
		Team team,
		AuthedUser authedUser
) {

	public record Team(String id, String name) {
	}

	public record AuthedUser(String id, String accessToken) {
	}
}
