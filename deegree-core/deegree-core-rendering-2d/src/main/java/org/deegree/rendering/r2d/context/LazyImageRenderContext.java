package org.deegree.rendering.r2d.context;

import static org.slf4j.LoggerFactory.getLogger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import org.deegree.rendering.r2d.Copyright;
import org.deegree.rendering.r2d.LabelRenderer;
import org.deegree.rendering.r2d.RasterRenderer;
import org.deegree.rendering.r2d.Renderer;
import org.deegree.rendering.r2d.TextRenderer;
import org.deegree.rendering.r2d.TileRenderer;
import org.slf4j.Logger;

public class LazyImageRenderContext implements RenderContext {

	private static final Logger LOG = getLogger(LazyImageRenderContext.class);

	private RenderContext renderContext;

	private MapOptions options;

	private final RenderingInfo info;

	private final OutputStream outputStream;

	public LazyImageRenderContext(RenderingInfo info, OutputStream outputStream) {
		this.info = info;
		this.outputStream = outputStream;
	}

	private RenderContext getRenderContext() {
		if (renderContext == null) {
			LOG.debug("Constructing ImageRenderContext with empty image");

			renderContext = ImageRenderContext.createInstance(info, outputStream);
			applyOptions();
		}

		return renderContext;
	}

	@Override
	public Renderer getVectorRenderer() {
		LOG.trace("Obtaining vector renderer");

		return getRenderContext().getVectorRenderer();
	}

	@Override
	public TextRenderer getTextRenderer() {
		LOG.trace("Obtaining text renderer");

		return getRenderContext().getTextRenderer();
	}

	@Override
	public LabelRenderer getLabelRenderer() {
		LOG.trace("Obtaining label renderer");

		return getRenderContext().getLabelRenderer();
	}

	@Override
	public RasterRenderer getRasterRenderer() {
		LOG.trace("Obtaining raster renderer");

		return getRenderContext().getRasterRenderer();
	}

	@Override
	public TileRenderer getTileRenderer() {
		LOG.trace("Obtaining tile renderer");

		return getRenderContext().getTileRenderer();
	}

	@Override
	public void optimizeAndDrawLabels() {
		LOG.trace("Optimize and draw labels");

		getRenderContext().optimizeAndDrawLabels();

	}

	@Override
	public void paintImage(BufferedImage img) {
		LOG.trace("Paint image");

		if (renderContext == null) {
			LOG.debug("Constructing ImageRenderContext with provided image");

			renderContext = ImageRenderContext.createInstance(info, img, outputStream);
			applyOptions();
		}
		else {
			renderContext.paintImage(img);
		}
	}

	@Override
	public void paintCopyright(Copyright copyright, int mapHeight) {
		LOG.trace("Paint copyright");

		getRenderContext().paintCopyright(copyright, mapHeight);
	}

	@Override
	public boolean close() throws IOException {
		LOG.trace("Closing render context");

		return getRenderContext().close();
	}

	private void applyOptions() {
		if (options != null) {
			LOG.trace("Delayed applying options");

			renderContext.applyOptions(options);
		}
	}

	@Override
	public void applyOptions(MapOptions options) {
		if (renderContext == null) {
			LOG.trace("Delaying apply options");

			this.options = options;
		}
		else {
			LOG.trace("Applying options");

			renderContext.applyOptions(options);
		}
	}

}
