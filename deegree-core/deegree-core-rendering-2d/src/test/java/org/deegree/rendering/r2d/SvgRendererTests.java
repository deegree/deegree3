package org.deegree.rendering.r2d;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import org.deegree.style.styling.components.Graphic;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class SvgRendererTests {

	private static final Logger LOG = LoggerFactory.getLogger(SvgRendererTests.class);

	@Parameters(name = "{index}: {4} {2}x{3} => {0}x{1}")
	public static Collection<Object[]> data() {

		return Arrays.asList(new Object[][] {
				// target width, height, rectWidth, rectHeight, file
				//
				{ 183, 100, 0, 100, "svg_w200_h100_border10.svg" }, //
				{ 100, 55, 100, 0, "svg_w200_h100_border10.svg" }, //
				{ 100, 100, 100, 100, "svg_w200_h100_border10.svg" }, //
				{ 220, 120, 0, 0, "svg_w200_h100_border10.svg" }, //
				{ 200, 100, 0, 100, "svg_w200_h100_no_border.svg" }, //
				{ 100, 50, 100, 0, "svg_w200_h100_no_border.svg" }, //
				{ 100, 100, 100, 100, "svg_w200_h100_no_border.svg" }, //
				{ 200, 100, 0, 0, "svg_w200_h100_no_border.svg" }, //
				//
				{ 100, 183, 100, 0, "svg_w100_h200_border10.svg" }, //
				{ 55, 100, 0, 100, "svg_w100_h200_border10.svg" }, //
				{ 100, 100, 100, 100, "svg_w100_h200_border10.svg" }, //
				{ 120, 220, 0, 0, "svg_w100_h200_border10.svg" }, //
				{ 100, 200, 100, 0, "svg_w100_h200_no_border.svg" }, //
				{ 50, 100, 0, 100, "svg_w100_h200_no_border.svg" }, //
				{ 100, 100, 100, 100, "svg_w100_h200_no_border.svg" }, //
				{ 100, 200, 0, 0, "svg_w100_h200_no_border.svg" }, //
				// prevent error with sizes that could get 0x0 pixels
				{ 1, 1, 0.51, 0.51, "svg_w100_h200_no_border.svg" },
				{ 1, 1, 0.50, 0.50, "svg_w100_h200_no_border.svg" },
				{ 1, 1, 0.49, 0.49, "svg_w100_h200_no_border.svg" },
				{ 1, 1, 0.2, 0.2, "svg_w100_h200_no_border.svg" }, });
	}

	@Parameter(0)
	public int requiredWidth;

	@Parameter(1)
	public int requiredHeight;

	@Parameter(2)
	public double requestedWidth;

	@Parameter(3)
	public double requestedHeight;

	@Parameter(4)
	public String fileName;

	@Test
	public void testGeneratedImage() throws IOException {
		Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, requestedWidth, requestedHeight);
		Graphic g = new Graphic();
		//
		g.size = requestedHeight > 0 ? requestedHeight : -requestedWidth;
		g.imageURL = getClass().getResource("svgtests/" + fileName).toExternalForm();

		BufferedImage img = (new SvgRenderer()).prepareSvg(rect, g);

		assertNotNull(img);
		LOG.info("generated image w: {} h: {} from: {}", img.getWidth(), img.getHeight(), fileName);
		assertEquals(requiredWidth, img.getWidth());
		assertEquals(requiredHeight, img.getHeight());
	}

}
