package org.deegree.rendering.r2d.context;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

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

    public Java2DRenderContext( RenderingInfo info, Graphics2D graphics, OutputStream outputStream ) {
        this.graphics = graphics;
        this.outputStream = outputStream;

        renderer = new Java2DRenderer( graphics, info.getWidth(), info.getHeight(), info.getEnvelope(),
                                       info.getPixelSize() * 1000 );
        textRenderer = new Java2DTextRenderer( renderer );
        labelRenderer = new Java2DLabelRenderer( renderer, textRenderer );
        rasterRenderer = new Java2DRasterRenderer( graphics );
        tileRenderer = new Java2DTileRenderer( graphics, info.getWidth(), info.getHeight(), info.getEnvelope() );
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
            new AutoLabelPlacement( labelRenderer.getLabels(), renderer );
        } catch ( Throwable e ) {
            e.printStackTrace();
        }
        labelRenderer.render();
    }

    @Override
    public void paintImage( BufferedImage img ) {
        graphics.drawImage( img, 0, 0, null );
    }

    @Override
    public void paintCopyright( String copyright, int mapHeight ) {
        if ( copyright != null ) {
            graphics.setFont( new Font( "SANSSERIF", Font.PLAIN, 14 ) );
            graphics.setColor( Color.BLACK );
            graphics.drawString( copyright, 8, mapHeight - 15 );
            graphics.drawString( copyright, 10, mapHeight - 15 );
            graphics.drawString( copyright, 8, mapHeight - 13 );
            graphics.drawString( copyright, 10, mapHeight - 13 );
            graphics.setColor( Color.WHITE );
            graphics.setFont( new Font( "SANSSERIF", Font.PLAIN, 14 ) );
            graphics.drawString( copyright, 9, mapHeight - 14 );
        }
    }

    @Override
    public boolean close()
                            throws IOException {
        graphics.dispose();
        return true;
    }
}
