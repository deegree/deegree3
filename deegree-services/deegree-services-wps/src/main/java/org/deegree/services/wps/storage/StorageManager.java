/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.services.wps.storage;

import java.io.File;
import java.io.IOException;

import org.deegree.commons.utils.io.StreamBufferStore;
import org.deegree.services.wps.ExecutionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides storage locations for process response documents and outputs (and sinks for
 * complex inputs).
 *
 * @see ExecutionManager
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class StorageManager {

	private static final Logger LOG = LoggerFactory.getLogger(StorageManager.class);

	private File baseDir;

	private long LAST_OUTPUT_ID = System.currentTimeMillis();

	private long LAST_RESPONSE_ID = System.currentTimeMillis();

	private long LAST_INPUT_ID = System.currentTimeMillis();

	private static final String OUTPUT_PREFIX = "wps_output_";

	private static final String RESPONSE_PREFIX = "wps_response_";

	private static final String INPUT_PREFIX = "wps_input_";

	private final int inputDiskSwitchLimit;

	/**
	 * Creates a new {@link StorageManager} instance.
	 * @param baseDir base directory where the resources are stored on the filesystem
	 * @param inputDiskSwitchLimit number of bytes allowed for each embedded complex input
	 * in memory
	 */
	public StorageManager(File baseDir, int inputDiskSwitchLimit) {
		LOG.info("Using directory '" + baseDir + "' for publishing complex outputs and response documents.");
		if (!baseDir.exists()) {
			LOG.error("Configured WPS storage directory name '" + baseDir
					+ "' does not exist. Please create this directory or adapt the WPS configuration.");
		}
		if (!baseDir.isDirectory()) {
			LOG.error("Configured WPS resource directory name '" + baseDir
					+ "' is not a directory. Please create this directory or adapt the WPS configuration.");
		}
		this.baseDir = baseDir;
		this.inputDiskSwitchLimit = inputDiskSwitchLimit;
	}

	public synchronized OutputStorage newOutputStorage(String mimeType) throws IOException {
		LOG.debug("Allocating new storage location for publishing output parameter.");
		String outputId = generateOutputId();
		String resourceName = OUTPUT_PREFIX + outputId;
		File resourceFile = new File(baseDir, resourceName);
		if (resourceFile.exists()) {
			LOG.debug("File '" + resourceFile + "' already exists. Deleting it.");
			resourceFile.delete();
		}
		return new OutputStorage(resourceFile, outputId, mimeType);
	}

	public ResponseDocumentStorage newResponseDocumentStorage(String getUrl) {
		LOG.debug("Allocating new storage location for publishing response document.");
		String responseId = generateResponseId();
		String resourceName = RESPONSE_PREFIX + responseId;
		File resourceFile = new File(baseDir, resourceName);
		if (resourceFile.exists()) {
			LOG.debug("File '" + resourceFile + "' already exists. Deleting it.");
			resourceFile.delete();
		}
		return new ResponseDocumentStorage(resourceFile, responseId, getUrl);
	}

	public OutputStorage lookupOutputStorage(String outputId) {
		OutputStorage output = null;
		File resourceFile = new File(baseDir, OUTPUT_PREFIX + outputId);
		try {
			if (resourceFile.exists()) {
				output = new OutputStorage(resourceFile, outputId);
			}
		}
		catch (IOException e) {
			LOG.debug("Cannot access stored output (file='" + resourceFile + "')");
		}
		return output;
	}

	public synchronized StreamBufferStore newInputSink() {
		LOG.debug("Allocating new sink for temporarily storing complex input parameter.");
		String outputId = generateInputId();
		String resourceName = INPUT_PREFIX + outputId;
		File resourceFile = new File(baseDir, resourceName);
		if (resourceFile.exists()) {
			LOG.debug("File '" + resourceFile + "' already exists. Deleting it.");
			resourceFile.delete();
		}
		return new StreamBufferStore(inputDiskSwitchLimit, resourceFile);
	}

	public ResponseDocumentStorage lookupResponseDocumentStorage(String responseId, String getUrl) {
		File resourceFile = new File(baseDir, RESPONSE_PREFIX + responseId);
		return new ResponseDocumentStorage(resourceFile, responseId, getUrl);
	}

	private synchronized String generateOutputId() {
		return Long.toHexString(LAST_OUTPUT_ID++);
	}

	private synchronized String generateResponseId() {
		return Long.toHexString(LAST_RESPONSE_ID++);
	}

	private synchronized String generateInputId() {
		return Long.toHexString(LAST_INPUT_ID++);
	}

}
