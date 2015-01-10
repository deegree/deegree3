package org.deegree.rendering.r2d.context;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import org.deegree.rendering.r2d.LabelRenderer;
import org.deegree.rendering.r2d.RasterRenderer;
import org.deegree.rendering.r2d.Renderer;
import org.deegree.rendering.r2d.TextRenderer;
import org.deegree.rendering.r2d.TileRenderer;

public class LazyImageRenderContext implements RenderContext {

    private RenderContext renderContext;
    
    private final RenderingInfo info;
    
    private final OutputStream outputStream;
    
    public LazyImageRenderContext( RenderingInfo info, OutputStream outputStream ) {
        this.info = info;
        this.outputStream = outputStream;
    }
    
    public RenderContext getRenderContext() {
        if(renderContext == null) {
            renderContext = ImageRenderContext.createInstance( info, outputStream );
        }
        
        return renderContext;
    }

    @Override
    public Renderer getVectorRenderer() {
        return getRenderContext().getVectorRenderer();
    }

    @Override
    public TextRenderer getTextRenderer() {
        return getRenderContext().getTextRenderer();
    }

    @Override
    public LabelRenderer getLabelRenderer() {
        return getRenderContext().getLabelRenderer();
    }

    @Override
    public RasterRenderer getRasterRenderer() {
       return getRenderContext().getRasterRenderer();
    }

    @Override
    public TileRenderer getTileRenderer() {
        return getRenderContext().getTileRenderer();
    }

    @Override
    public void optimizeAndDrawLabels() {
        getRenderContext().optimizeAndDrawLabels();
        
    }

    @Override
    public void paintImage( BufferedImage img ) {
        if(renderContext == null) {
            renderContext = ImageRenderContext.createInstance( info, img, outputStream );
        } else {
            renderContext.paintImage( img );
        }
    }

    @Override
    public boolean close()
                            throws IOException {
        return getRenderContext().close();
    }

    @Override
    public void applyOptions( MapOptions options ) {
        getRenderContext().applyOptions( options );        
    }
}
