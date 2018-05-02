package org.springframework.ingest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration Properties for BatchConfiguration
 *
 * @author Chris Schaefer
 */
@ConfigurationProperties
public class BatchConfigurationProperties {
	private String sftpHost;
	private Integer sftpPort;
	private String sftpUsername;
	private String sftpPassword;

	public String getSftpHost() {
		return sftpHost;
	}

	public void setSftpHost(String sftpHost) {
		this.sftpHost = sftpHost;
	}

	public Integer getSftpPort() {
		return sftpPort;
	}

	public void setSftpPort(Integer sftpPort) {
		this.sftpPort = sftpPort;
	}

	public String getSftpUsername() {
		return sftpUsername;
	}

	public void setSftpUsername(String sftpUsername) {
		this.sftpUsername = sftpUsername;
	}

	public String getSftpPassword() {
		return sftpPassword;
	}

	public void setSftpPassword(String sftpPassword) {
		this.sftpPassword = sftpPassword;
	}
}
