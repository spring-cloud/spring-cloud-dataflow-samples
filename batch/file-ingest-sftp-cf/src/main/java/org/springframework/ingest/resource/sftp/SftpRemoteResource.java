package org.springframework.ingest.resource.sftp;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.ingest.resource.RemoteResource;
import org.springframework.integration.file.remote.InputStreamCallback;
import org.springframework.integration.file.remote.RemoteFileOperations;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.util.Assert;

/**
 * RemoteResource implementation utilizing a Spring Integration SftpRemoteFileTemplate
 * to connect and return a file as a Spring Resource.
 *
 * @author Chris Schaefer
 */
public class SftpRemoteResource implements RemoteResource {
	private static final Integer DEFAULT_SFTP_PORT = 22;
	private static final String LOCALHOST = "127.0.0.1";

	private final String host;
	private final Integer port;
	private final String username;
	private final String password;

	public SftpRemoteResource(String host, Integer port, String username, String password) {
		Assert.hasText(username, "Username must be defined");
		Assert.hasText(password, "Password must be defined");

		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}

	@Override
	public Resource getResource(String resourceLocation) {
		DefaultSftpSessionFactory sessionFactory = getSessionFactory();
		RemoteFileOperations remoteFileOperations = new SftpRemoteFileTemplate(sessionFactory);

		FileFetcher filefetcher = new FileFetcher();
		remoteFileOperations.get(resourceLocation, filefetcher);

		return new ByteArrayResource(filefetcher.getBytes());
	}

	private DefaultSftpSessionFactory getSessionFactory() {
		DefaultSftpSessionFactory sessionFactory = new DefaultSftpSessionFactory();
		sessionFactory.setHost(host != null ? host : LOCALHOST);
		sessionFactory.setPort(port != null ? port : DEFAULT_SFTP_PORT);
		sessionFactory.setUser(username);
		sessionFactory.setPassword(password);
		sessionFactory.setAllowUnknownKeys(true);

		return sessionFactory;
	}


	private static class FileFetcher implements InputStreamCallback {
		private byte[] bytes;

		@Override
		public void doWithInputStream(InputStream inputStream) {
			try {
				bytes = IOUtils.toByteArray(inputStream);
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to convert InputStream to byte array", e);
			}
		}

		public byte[] getBytes() {
			return bytes;
		}
	}
}
