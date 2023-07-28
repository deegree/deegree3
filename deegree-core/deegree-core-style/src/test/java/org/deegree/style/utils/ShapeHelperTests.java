package org.deegree.style.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.StringReader;
import org.junit.Before;
import org.junit.Test;
import org.postgresql.util.ReaderInputStream;

public class ShapeHelperTests {

	private final String TUNABLE_OLD_SCALE = "deegree.rendering.svg-to-shape.previous";

	@Before
	public void resetTunable() {
		ShapeHelper.SVG_TO_SHAPE_FALLBACK = false;
	}

	// NOTE this test can be removed if the fallback is removed form ShapeHelper
	@Test
	public void testSvgToShapeConversionViewboxWidthHeightFallbackBad() {
		ShapeHelper.SVG_TO_SHAPE_FALLBACK = true;
		String svg;
		svg = "<svg width=\"61.2\" height=\"59.4\" version=\"1.1\" viewBox=\"0 0 16.2 15.7\" xmlns=\"http://www.w3.org/2000/svg\">\n";
		svg += " <rect x=\".132\" y=\".132\" width=\"11.1\" height=\"10.5\" fill=\"#ff0\" stroke=\"#ff0\" stroke-width=\".265\"/>\n";
		svg += " <ellipse cx=\"13.6\" cy=\"13.1\" rx=\"2.42\" ry=\"2.49\" fill=\"#808000\" stroke=\"#808000\" stroke-width=\".254\"/>\n";
		svg += "</svg>\n";

		Shape shp = ShapeHelper.getShapeFromSvg(new ReaderInputStream(new StringReader(svg)), "dummy.svg");
		Rectangle2D bounds = shp.getBounds2D();

		assertThat(bounds.getMinX(), closeTo(0.0d, 0.02));
		assertThat(bounds.getMinY(), closeTo(0.0d, 0.02));
		assertThat(bounds.getMaxX(), closeTo(0.02d, 0.02));
		assertThat(bounds.getMaxY(), closeTo(0.02d, 0.02));
	}

	@Test
	public void testSvgToShapeConversionViewboxWidthHeight() {
		String svg;
		svg = "<svg width=\"61.2\" height=\"59.4\" version=\"1.1\" viewBox=\"0 0 16.2 15.7\" xmlns=\"http://www.w3.org/2000/svg\">\n";
		svg += " <rect x=\".132\" y=\".132\" width=\"11.1\" height=\"10.5\" fill=\"#ff0\" stroke=\"#ff0\" stroke-width=\".265\"/>\n";
		svg += " <ellipse cx=\"13.6\" cy=\"13.1\" rx=\"2.42\" ry=\"2.49\" fill=\"#808000\" stroke=\"#808000\" stroke-width=\".254\"/>\n";
		svg += "</svg>\n";

		Shape shp = ShapeHelper.getShapeFromSvg(new ReaderInputStream(new StringReader(svg)), "dummy.svg");
		Rectangle2D bounds = shp.getBounds2D();

		// bounds should be 1 x 1 in size
		assertThat(bounds.getMinX(), closeTo(0.0d, 0.05));
		assertThat(bounds.getMinY(), closeTo(0.0d, 0.05));
		assertThat(bounds.getMaxX(), closeTo(1.0d, 0.05));
		assertThat(bounds.getMaxY(), closeTo(1.0d, 0.05));
	}

	@Test
	public void testSvgToShapeConversionWidthHeight() {
		String svg;
		svg = "<svg width=\"16.2\" height=\"15.7\" xmlns=\"http://www.w3.org/2000/svg\">\n";
		svg += " <rect x=\".132\" y=\".132\" width=\"11.1\" height=\"10.5\" fill=\"#ff0\" stroke=\"#ff0\" stroke-width=\".265\"/>\n";
		svg += " <ellipse cx=\"13.6\" cy=\"13.1\" rx=\"2.42\" ry=\"2.49\" fill=\"#808000\" stroke=\"#808000\" stroke-width=\".254\"/>\n";
		svg += "</svg>\n";

		Shape shp = ShapeHelper.getShapeFromSvg(new ReaderInputStream(new StringReader(svg)), "dummy.svg");
		Rectangle2D bounds = shp.getBounds2D();

		// bounds should be 1 x 1 in size
		assertThat(bounds.getMinX(), closeTo(0.0d, 0.05));
		assertThat(bounds.getMinY(), closeTo(0.0d, 0.05));
		assertThat(bounds.getMaxX(), closeTo(1.0d, 0.05));
		assertThat(bounds.getMaxY(), closeTo(1.0d, 0.05));
	}

	@Test
	public void testSvgToShapeConversionViewbox() {
		String svg;
		svg = "<svg viewBox=\"0 0 16.2 15.7\" xmlns=\"http://www.w3.org/2000/svg\">\n";
		svg += " <rect x=\".132\" y=\".132\" width=\"11.1\" height=\"10.5\" fill=\"#ff0\" stroke=\"#ff0\" stroke-width=\".265\"/>\n";
		svg += " <ellipse cx=\"13.6\" cy=\"13.1\" rx=\"2.42\" ry=\"2.49\" fill=\"#808000\" stroke=\"#808000\" stroke-width=\".254\"/>\n";
		svg += "</svg>\n";

		Shape shp = ShapeHelper.getShapeFromSvg(new ReaderInputStream(new StringReader(svg)), "dummy.svg");
		Rectangle2D bounds = shp.getBounds2D();

		// bounds should be 1 x 1 in size
		assertThat(bounds.getMinX(), closeTo(0.0d, 0.05));
		assertThat(bounds.getMinY(), closeTo(0.0d, 0.05));
		assertThat(bounds.getMaxX(), closeTo(1.0d, 0.05));
		assertThat(bounds.getMaxY(), closeTo(1.0d, 0.05));
	}

}
