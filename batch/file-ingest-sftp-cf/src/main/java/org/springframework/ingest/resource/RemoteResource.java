package org.springframework.ingest.resource;

import org.springframework.core.io.Resource;

/**
 * Interface definition for remote Resource implementations.
 *
 * @author Chris Schaefer
 */
public interface RemoteResource {
	Resource getResource(String resourceLocation);
}
