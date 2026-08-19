package com.likelion.yonsei.baton;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class BatonApplication {

	public static void main(String[] args) {
		// Pin the JVM default timezone to UTC so Hibernate's @CreationTimestamp/@UpdateTimestamp
		// (which use the ambient JVM clock, not our injected UTC Clock bean) stay consistent with
		// every explicitly Clock-based timestamp in the app. See AGENTS.md "시간 처리".
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(BatonApplication.class, args);
	}

}
