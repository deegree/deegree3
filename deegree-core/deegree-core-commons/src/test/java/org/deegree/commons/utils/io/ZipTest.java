package org.deegree.commons.utils.io;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.fail;

public class ZipTest {

	@Test
	public void testUnzipWithInvalidEntry() {
		File targetDir = new File(System.getProperty("java.io.tmpdir"), "ziptest_invalid");
		targetDir.mkdirs();
		try {
			InputStream in = invalidateEntryPath();
			Zip.unzip(in, targetDir, false);
			fail("Expected IOException for invalid zip entry path");
		}
		catch (IOException e) {
			// expected
		}
	}

	private InputStream invalidateEntryPath() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(baos);
		ZipEntry entry = new ZipEntry("../invalid.txt");
		zos.putNextEntry(entry);
		zos.write("malicious".getBytes());
		zos.closeEntry();
		zos.close();
		return new ByteArrayInputStream(baos.toByteArray());
	}

}
