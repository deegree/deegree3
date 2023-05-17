/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.commons.utils.test;

import static org.deegree.commons.utils.io.Utils.determineSimilarity;

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>IntegrationTestUtils</code>
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */

public class IntegrationTestUtils {

	private static final Logger LOG = LoggerFactory.getLogger(IntegrationTestUtils.class);

	/**
	 * Converts rendered image into byte array
	 */
	private static byte[] toBytes(RenderedImage image, String format) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, format, baos);
		baos.flush();
		baos.close();
		return baos.toByteArray();
	}

	/**
	 * Create Base64 encoded text of a ZIP-Archive containing the passed binary data/file
	 */
	public static String toBase64Zip(byte[] data, String filename) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ZipOutputStream zip = new ZipOutputStream(baos)) {
			zip.putNextEntry(new ZipEntry(filename));
			IOUtils.write(data, zip);
			zip.closeEntry();
			zip.flush();
			zip.finish();
			baos.flush();
			byte[] gzipped = baos.toByteArray();
			return StringUtils.newStringUtf8(Base64.encodeBase64Chunked(gzipped));
		}
		catch (IOException ex) {
			ex.printStackTrace();
			return "Encoding failed with: " + ex.getMessage();
		}
	}

	/**
	 * Stores the data on pastebin server
	 *
	 * If a server url for put request is configured with the environment vairable
	 * <code>PASTEBIN_PUT_URL</code>, the data is sent to that url an the result (file
	 * address is returned)
	 * @param data
	 * @return url on the server or <code>null</code> if no server address is configured
	 * or an error occurred
	 */
	public static String toPasteBin(byte[] data) {
		String pasteBinUrl = System.getenv("PASTEBIN_PUT_URL");
		if (pasteBinUrl == null || pasteBinUrl.isEmpty())
			return null;

		HttpRequest request = HttpRequest.newBuilder() //
			.timeout(Duration.ofMinutes(2)) //
			.uri(URI.create(pasteBinUrl)) //
			.PUT(HttpRequest.BodyPublishers.ofByteArray(data))//
			.build();
		return HttpClient.newHttpClient() //
			.sendAsync(request, HttpResponse.BodyHandlers.ofString()) //
			.thenApply(response -> {
				if (response.statusCode() == 200) {
					return response.body();
				}
				LOG.warn("Publishing data on pastebin failed with return code {}", response.statusCode());
				return null;
			}) //
			.join();
	}

	/**
	 * Stores the expected and actual image of a comparsion into the temporary directory
	 * @param expected The expected image
	 * @param actual The actual image
	 * @param name Name of the Test
	 */
	private static void toTempfile(RenderedImage expected, RenderedImage actual, String name) {
		try {
			Path tempDir = Path.of(System.getProperty("java.io.tmpdir"));

			LOG.error("Trying to store {}_expected/{}_actual in java.io.tmpdir", name, name);
			Files.write(tempDir.resolve(name + "_actual.png"), toBytes(actual, "png"));
			Files.write(tempDir.resolve(name + "_expected.png"), toBytes(expected, "png"));

			System.out.println("Result returned for " + name + " (base64 -di encoded.dat > failed-test.zip)");
			System.out.println(toBase64Zip(toBytes(actual, "png"), name + ".png"));
		}
		catch (Throwable t) {
		}
	}

	/**
	 * Checks if two images are similar
	 */
	public static boolean isImageSimilar(RenderedImage expected, RenderedImage actual, double maximumDifference,
			String name) throws Exception {
		double sim = determineSimilarity(expected, actual);

		if (Math.abs(1.0 - sim) > maximumDifference) {
			LOG.error("Images for test '{}' are not similar enough ({}>{})", name, sim, maximumDifference);

			String pasteBin = IntegrationTestUtils.toPasteBin(toBytes(actual, "png"));
			if (pasteBin != null) {
				System.out.println("Actual returned image for " + name + " available at " + pasteBin);
			}
			toTempfile(expected, actual, name);

			return false;
		}
		else {
			LOG.info("Similarity test '{}' ok ({})", name, 1.0 - sim);
			return true;
		}
	}

}
