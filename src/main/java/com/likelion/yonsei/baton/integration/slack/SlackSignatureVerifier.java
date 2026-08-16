package com.likelion.yonsei.baton.integration.slack;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.HexFormat;

/** Verifies Slack Events API request signatures per Slack's signing-secret scheme. */
@Component
public class SlackSignatureVerifier {

	private static final String HMAC_ALGORITHM = "HmacSHA256";
	private static final long MAX_CLOCK_SKEW_SECONDS = 60 * 5;

	private final SlackProperties properties;
	private final Clock clock;

	public SlackSignatureVerifier(SlackProperties properties, Clock clock) {
		this.properties = properties;
		this.clock = clock;
	}

	public boolean isValid(String timestampHeader, String signatureHeader, String rawBody) {
		if (timestampHeader == null || signatureHeader == null) {
			return false;
		}

		long timestamp;
		try {
			timestamp = Long.parseLong(timestampHeader);
		} catch (NumberFormatException e) {
			return false;
		}

		long now = clock.instant().getEpochSecond();
		if (Math.abs(now - timestamp) > MAX_CLOCK_SKEW_SECONDS) {
			return false;
		}

		String expected = "v0=" + hmacSha256(properties.signingSecret(), "v0:" + timestamp + ":" + rawBody);
		return constantTimeEquals(expected, signatureHeader);
	}

	private String hmacSha256(String secret, String message) {
		try {
			Mac mac = Mac.getInstance(HMAC_ALGORITHM);
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
			byte[] digest = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to compute Slack signature", e);
		}
	}

	private boolean constantTimeEquals(String a, String b) {
		if (a.length() != b.length()) {
			return false;
		}
		int result = 0;
		for (int i = 0; i < a.length(); i++) {
			result |= a.charAt(i) ^ b.charAt(i);
		}
		return result == 0;
	}
}
