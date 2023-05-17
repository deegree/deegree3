/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.test.utils;

import static java.io.File.separatorChar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.deegree.commons.utils.TempFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the setting up of clean instances and the starting and stopping of Tomcat.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class TomcatHelper {

	private static final Logger LOG = LoggerFactory.getLogger(TomcatHelper.class);

	private final int port = 18080;

	private final int STATUS_CHANGE_WAIT = 60 * 1000;

	private final File tempDir = TempFileManager.getBaseDir();

	private final String tomcatDownloadURL = "http://download.deegree.org/test-artefacts/tomcat-6-p18080.zip";

	private final String tomcatArchive = tempDir.getAbsolutePath() + separatorChar + "tomcat-6-p18080.zip";

	private final String checkStatusURL = "http://127.0.0.1:" + port;

	private final String tomcatBaseDir = tempDir.getAbsolutePath() + separatorChar + "tomcat-6-p18080";

	private Process tomcatProcess;

	/**
	 * Sets up a vanilla Tomcat instance, ready to be started.
	 * <p>
	 * If Tomcat is already running, it is stopped and removed first.
	 * </p>
	 * @throws IOException
	 */
	public void setup() throws IOException {
		if (isRunning()) {
			stop();
		}
		remove();
		if (!new File(tomcatArchive).exists()) {
			downloadArchive();
		}
		unzipArchive();
	}

	public void deploy(File src) {

	}

	private void downloadArchive() throws IOException {

		LOG.info("Downloading '" + tomcatDownloadURL + "' into '" + tomcatArchive + "'");
		BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(tomcatArchive));
		BufferedInputStream is = new BufferedInputStream(new URL(tomcatDownloadURL).openStream());

		byte[] buffer = new byte[10240];
		int bytesRead = 0;
		while ((bytesRead = is.read(buffer)) > 0) {
			os.write(buffer, 0, bytesRead);
		}
		is.close();
		os.close();
		LOG.info("Done.");
	}

	private void unzipArchive() {
		LOG.info("Unzipping '" + tomcatArchive + "'");
		if (!new File(tomcatBaseDir).mkdir()) {
			throw new RuntimeException("Unable to create Tomcat basedir: " + tomcatBaseDir);
		}
		try {
			ZipFile zipFile = new ZipFile(tomcatArchive);
			Enumeration entries = zipFile.entries();

			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				LOG.debug("Extracting '" + entry.getName() + "'");
				if (entry.isDirectory()) {
					if (!(new File(tomcatBaseDir, entry.getName())).mkdir()) {
						throw new IOException(
								"Unable to create directory '" + new File(tomcatBaseDir, entry.getName()) + "'.");
					}
					continue;
				}

				// extract file
				InputStream is = zipFile.getInputStream(entry);
				OutputStream os = new BufferedOutputStream(
						new FileOutputStream(new File(tomcatBaseDir, entry.getName())));
				byte[] buffer = new byte[10240];
				int bytesRead = 0;
				while ((bytesRead = is.read(buffer)) > 0) {
					os.write(buffer, 0, bytesRead);
				}
				is.close();
				os.close();
			}

			zipFile.close();
		}
		catch (IOException ioe) {
			System.err.println("Unhandled exception:");
			ioe.printStackTrace();
			return;
		}
		LOG.info("done.");
	}

	public void remove() {
		if (isRunning()) {
			stop();
		}
		File file = new File(tomcatBaseDir);
		if (file.exists()) {
			if (!deleteDir(file)) {
				throw new RuntimeException("Unable to delete Tomcat directory: " + tomcatBaseDir);
			}
		}
	}

	private boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}

	private void stop() {

		if (!isRunning()) {
			throw new RuntimeException("Cannot stop Tomcat. Already stopped.");
		}

		try {
			String cmd = "java -Dcatalina.home=" + tomcatBaseDir + " -jar " + tomcatBaseDir + separatorChar + "bin"
					+ separatorChar + "bootstrap.jar stop";
			LOG.info("Shutting down tomcat: " + cmd);

			long started = System.currentTimeMillis();
			while (System.currentTimeMillis() - started < STATUS_CHANGE_WAIT) {
				Runtime.getRuntime().exec(cmd);
				if (!isRunning()) {
					LOG.info("Successfully stopped Tomcat.");
					return;
				}
				LOG.debug("Still running.");
				Thread.sleep(1 * 1000);
			}
			String msg = "Unable to stop Tomcat.";
			throw new RuntimeException(msg);
		}
		catch (Exception e) {
			String msg = "Unable to stop Tomcat: " + e.getMessage();
			throw new RuntimeException(msg);
		}
	}

	private void start() {

		if (isRunning()) {
			throw new RuntimeException("Unable to start Tomcat. Already running.");
		}

		try {
			String cmd = "java -Dcatalina.home=" + tomcatBaseDir + " -jar " + tomcatBaseDir + separatorChar + "bin"
					+ separatorChar + "bootstrap.jar";
			LOG.info("Starting up tomcat: " + cmd);
			tomcatProcess = Runtime.getRuntime().exec(cmd);

			long started = System.currentTimeMillis();
			while (System.currentTimeMillis() - started < STATUS_CHANGE_WAIT) {
				if (isRunning()) {
					LOG.info("Successfully started Tomcat.");
					return;
				}
				LOG.debug("Not running.");
				Thread.sleep(1 * 1000);
			}
			String msg = "Unable to start Tomcat.";
			throw new RuntimeException(msg);
		}
		catch (Exception e) {
			String msg = "Unable to start Tomcat: " + e.getMessage();
			throw new RuntimeException(msg);
		}
	}

	private boolean isRunning() {
		try {
			LOG.debug("Retrieving " + checkStatusURL + " to see if Tomcat is up and running.");
			URL url = new URL(checkStatusURL);
			URLConnection urlConn = url.openConnection();
			urlConn.connect();
			LOG.debug("yes.");
		}
		catch (MalformedURLException e) {
			// should not happen
			LOG.error("Internal error: malformed status URL: " + checkStatusURL);
		}
		catch (IOException e) {
			LOG.debug("no.");
			return false;
		}
		return true;
	}

	public static void main(String[] args) throws IOException, InterruptedException {

		for (int i = 0; i < 10; i++) {
			new TomcatHelper().setup();
			new TomcatHelper().start();
			new TomcatHelper().stop();
		}
	}

}
