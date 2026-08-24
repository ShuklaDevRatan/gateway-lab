package com.drs.service_config_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class ServiceConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceConfigServerApplication.class, args);
	}

}
