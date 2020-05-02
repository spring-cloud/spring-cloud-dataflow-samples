package com.example.timesource;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TimeSourceApplication {

	@Bean
	public Supplier<String> timeSupplier() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		return () -> {
			return sdf.format(new Date());
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(TimeSourceApplication.class, args);
	}

}
