package com.likelion.yonsei.baton.common.crypto;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Generates opaque per-user API keys and hashes them for storage/lookup.
 * Only the SHA-256 hash is ever persisted; the raw key is shown to the caller once, at signup.
 */
@Component
public class ApiKeyGenerator {

	private static final int KEY_BYTES = 32;

	private final SecureRandom secureRandom = new SecureRandom();

	public String generate() {
		byte[] bytes = new byte[KEY_BYTES];
		secureRandom.nextBytes(bytes);
		return "baton_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	public String hash(String apiKey) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(apiKey.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (Exception e) {
			throw new IllegalStateException("SHA-256 not available", e);
		}
	}
}
