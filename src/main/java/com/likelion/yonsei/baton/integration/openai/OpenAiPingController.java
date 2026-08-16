package com.likelion.yonsei.baton.integration.openai;

import com.likelion.yonsei.baton.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenAiPingController {

	private final OpenAiClient openAiClient;

	public OpenAiPingController(OpenAiClient openAiClient) {
		this.openAiClient = openAiClient;
	}

	@GetMapping("/api/integrations/openai/ping")
	public ApiResponse<String> ping(@RequestParam(defaultValue = "안녕하세요! 한 문장으로만 답해주세요.") String prompt) {
		return ApiResponse.success(openAiClient.chat(prompt));
	}
}
