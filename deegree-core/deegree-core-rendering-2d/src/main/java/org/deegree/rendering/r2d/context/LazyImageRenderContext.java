package org.deegree.rendering.r2d.context;

import static org.slf4j.LoggerFactory.getLogger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import org.deegree.rendering.r2d.LabelRenderer;
import org.deegree.rendering.r2d.RasterRenderer;
import org.deegree.rendering.r2d.Renderer;
import org.deegree.rendering.r2d.TextRenderer;
import org.deegree.rendering.r2d.TileRenderer;
import org.slf4j.Logger;

public class LazyImageRenderContext implements RenderContext {
    
    private static final Logger LOG = getLogger( LazyImageRenderContext.class );

    private RenderContext renderContext;
    
    private final RenderingInfo info;
    
    private final OutputStream outputStream;
    
    public LazyImageRenderContext( RenderingInfo info, OutputStream outputStream ) {
        this.info = info;
        this.outputStream = outputStream;
    }
    
    private RenderContext getRenderContext() {
        if(renderContext == null) {
            LOG.debug( "Constructing ImageRenderContext with empty image" );
            
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
            LOG.debug( "Constructing ImageRenderContext with provided image" );
            
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
