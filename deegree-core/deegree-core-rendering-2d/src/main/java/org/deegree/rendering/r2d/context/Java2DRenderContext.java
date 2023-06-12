package org.deegree.rendering.r2d.context;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import org.deegree.rendering.r2d.Copyright;
import org.deegree.rendering.r2d.Java2DLabelRenderer;
import org.deegree.rendering.r2d.Java2DRasterRenderer;
import org.deegree.rendering.r2d.Java2DRenderer;
import org.deegree.rendering.r2d.Java2DTextRenderer;
import org.deegree.rendering.r2d.Java2DTileRenderer;
import org.deegree.rendering.r2d.labelplacement.AutoLabelPlacement;

public abstract class Java2DRenderContext implements RenderContext {

	protected final Graphics2D graphics;

	protected final OutputStream outputStream;

	protected final Java2DRenderer renderer;

	protected final Java2DTextRenderer textRenderer;

	protected final Java2DLabelRenderer labelRenderer;

	protected final Java2DRasterRenderer rasterRenderer;

	protected final Java2DTileRenderer tileRenderer;

	public Java2DRenderContext(RenderingInfo info, Graphics2D graphics, OutputStream outputStream) {
		this.graphics = graphics;
		this.outputStream = outputStream;

		renderer = new Java2DRenderer(graphics, info.getWidth(), info.getHeight(), info.getEnvelope(),
				info.getPixelSize() * 1000);
		textRenderer = new Java2DTextRenderer(renderer);
		labelRenderer = new Java2DLabelRenderer(renderer, textRenderer);
		rasterRenderer = new Java2DRasterRenderer(graphics);
		tileRenderer = new Java2DTileRenderer(graphics, info.getWidth(), info.getHeight(), info.getEnvelope());
	}

	@Override
	public Java2DRenderer getVectorRenderer() {
		return renderer;
	}

	@Override
	public Java2DTextRenderer getTextRenderer() {
		return textRenderer;
	}

	@Override
	public Java2DLabelRenderer getLabelRenderer() {
		return labelRenderer;
	}

	@Override
	public Java2DRasterRenderer getRasterRenderer() {
		return rasterRenderer;
	}

	@Override
	public Java2DTileRenderer getTileRenderer() {
		return tileRenderer;
	}

	@Override
	public void optimizeAndDrawLabels() {
		// Optimize Label Placement here, if pointplacement set to auto=true
		try {
			new AutoLabelPlacement(labelRenderer.getLabels(), renderer);
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
		labelRenderer.render();
	}

	@Override
	public void paintImage(BufferedImage img) {
		graphics.drawImage(img, 0, 0, null);
	}

	@Override
	public void paintCopyright(Copyright copyright, int mapHeight) {
		if (copyright != null) {
			String copyrightText = copyright.getCopyrightText();
			BufferedImage copyrightImage = copyright.getCopyrightImage();
			int offsetX = copyright.getOffsetX();
			int offsetY = copyright.getOffsetY();
			if (copyrightText != null) {
				drawCopyrightText(copyrightText, offsetX, offsetY, mapHeight);
			}
			else if (copyrightImage != null) {
				drawCopyrightImage(copyrightImage, offsetX, offsetY, mapHeight);
			}
		}
	}

	@Override
	public boolean close() throws IOException {
		graphics.dispose();
		return true;
	}

	private void drawCopyrightText(String copyright, int offsetX, int offsetY, int mapHeight) {
		graphics.setFont(new Font("SANSSERIF", Font.PLAIN, 14));
		graphics.setColor(Color.BLACK);
		graphics.drawString(copyright, offsetX, mapHeight - offsetY + 2);
		graphics.drawString(copyright, offsetX + 2, mapHeight - offsetY + 2);
		graphics.drawString(copyright, offsetX, mapHeight - offsetY);
		graphics.drawString(copyright, offsetX + 2, mapHeight - offsetY);
		graphics.setColor(Color.WHITE);
		graphics.setFont(new Font("SANSSERIF", Font.PLAIN, 14));
		graphics.drawString(copyright, offsetX + 1, mapHeight - offsetY + 1);
	}

	private void drawCopyrightImage(BufferedImage copyrightImage, int offsetX, int offsetY, int mapHeight) {
		int y = mapHeight - copyrightImage.getHeight() - offsetY;
		graphics.drawImage(copyrightImage, offsetX, y, null);
	}

}
