package com.example.customdataflowserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.cloud.dataflow.server.EnableDataFlowServer;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryDeployerAutoConfiguration;
import org.springframework.cloud.deployer.spi.kubernetes.KubernetesAutoConfiguration;
import org.springframework.cloud.deployer.spi.local.LocalDeployerAutoConfiguration;

@SpringBootApplication(exclude = {
	SessionAutoConfiguration.class,
	ManagementWebSecurityAutoConfiguration.class,
	SecurityAutoConfiguration.class,
	UserDetailsServiceAutoConfiguration.class,
	LocalDeployerAutoConfiguration.class,
	CloudFoundryDeployerAutoConfiguration.class,
	KubernetesAutoConfiguration.class
})
@EnableDataFlowServer
public class CustomDataflowServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomDataflowServerApplication.class, args);
	}
}
