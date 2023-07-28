package org.deegree.services.wms;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.deegree.commons.utils.net.HttpUtils;
import org.junit.Test;

public class GetFeatureInfoIntegrationTest {

	public static final String GFI_BASE = "/wms?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetFeatureInfo&QUERY_LAYERS=satellite_provo_scalelimit&SRS=EPSG%3A26912&LAYERS=satellite_provo_scalelimit&STYLES=default&FORMAT=image%2Fpng&TRANSPARENT=FALSE&INFO_FORMAT=text%2Fplain";

	@Test
	public void test96dpiNoResult() throws IOException {
		// layer has scale 1:6900 - 1:7100
		assertThat("1:6895 < min scale", get(GFI_BASE
				+ "&X=250&Y=250&WIDTH=500&HEIGHT=500&BBOX=445088.216145833,4448605.21614583,445735.783854167,4449252.78385417&DPI=96"),
				not(containsString("data:")));

		assertThat("1:7105 > max sacle", get(GFI_BASE
				+ "&X=250&Y=250&WIDTH=500&HEIGHT=500&BBOX=444942.033854167,4448459.03385417,445881.966145833,4449398.96614583&DPI=96"),
				not(containsString("data:")));

	}

	@Test
	public void test96dpiWithResult() throws IOException {
		// layer has scale 1:6900 - 1:7100
		assertThat("1:6905 > min scale", get(GFI_BASE
				+ "&X=250&Y=250&WIDTH=500&HEIGHT=500&BBOX=444955.263020833,4448472.26302083,445868.736979167,4449385.73697917&DPI=96"),
				containsString("data:"));

		assertThat("1:7095 < max scale", get(GFI_BASE
				+ "&X=250&Y=250&WIDTH=500&HEIGHT=500&BBOX=444942.6953125,4448459.6953125,445881.3046875,4449398.3046875&DPI=96"),
				containsString("data:"));
	}

	@Test
	public void test192dpiNoResult() throws IOException {
		// layer has scale 1:6900 - 1:7100
		assertThat("1:6895 < min scale", get(GFI_BASE
				+ "&X=500&Y=500&WIDTH=1000&HEIGHT=1000&BBOX=445088.216145833,4448605.21614583,445735.783854167,4449252.78385417&DPI=192"),
				not(containsString("data:")));

		assertThat("1:7105 > max sacle", get(GFI_BASE
				+ "&X=500&Y=500&WIDTH=1000&HEIGHT=1000&BBOX=444942.033854167,4448459.03385417,445881.966145833,4449398.96614583&DPI=192"),
				not(containsString("data:")));

	}

	@Test
	public void test192dpiWithResult() throws IOException {
		// layer has scale 1:6900 - 1:7100
		assertThat("1:6905 > min scale", get(GFI_BASE
				+ "&X=500&Y=500&WIDTH=1000&HEIGHT=1000&BBOX=444955.263020833,4448472.26302083,445868.736979167,4449385.73697917&DPI=192"),
				containsString("data:"));

		assertThat("1:7095 < max scale", get(GFI_BASE
				+ "&X=500&Y=500&WIDTH=1000&HEIGHT=1000&BBOX=444942.6953125,4448459.6953125,445881.3046875,4449398.3046875&DPI=192"),
				containsString("data:"));
	}

	private String get(String request) throws IOException {
		String base = "http://localhost:" + System.getProperty("portnumber", "8080") + "/";
		base += System.getProperty("deegree-wms-similarity-webapp", "deegree-wms-similarity-tests");
		base += "/services" + request;

		return HttpUtils.get(new HttpUtils.Worker<String>() {

			@Override
			public String work(InputStream in) throws IOException {
				try (Reader isr = new InputStreamReader(in, StandardCharsets.UTF_8)) {
					String text = new BufferedReader(isr).lines().collect(Collectors.joining("\n"));
					return text;
				}
			}

		}, base, null);
	}

}
