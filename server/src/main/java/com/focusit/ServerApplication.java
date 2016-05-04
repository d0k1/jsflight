package com.focusit;

import com.focusit.model.Settings;
import com.focusit.repository.SettingsRepository;
import com.focusit.service.SettingsService;
import org.bson.types.ObjectId;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import javax.inject.Inject;

import static com.focusit.model.Settings.SETTINGS_ID;

/**
 * Generic Spring Boot Application entry point
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan
@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.focusit.repository")
public class ServerApplication implements CommandLineRunner {
	@Inject
	SettingsService settingsService;

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
		settingsService.getSettings();
	}
}
