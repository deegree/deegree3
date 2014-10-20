package org.deegree.rendering.r2d;

import org.deegree.cs.CRSCodeType;
import org.deegree.geometry.Envelope;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.CRS;
import org.opengis.coverage.Coverage;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;

import javax.media.jai.NullOpImage;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.NoSuchElementException;

import static javax.media.jai.Interpolation.INTERP_BILINEAR;
import static javax.media.jai.Interpolation.getInstance;
import static javax.media.jai.OpImage.OP_IO_BOUND;
import static org.geotools.coverage.processing.Operations.DEFAULT;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Can be used to transform raster images. The transform mechanism uses geotools.
 *
 * @author <a href="mailto:stenger@lat-lon.de">Dirk Stenger</a>
 * @author last edited by: $Author: stenger $
 * @version $Revision: $, $Date: $
 */
public class GeotoolsRasterTransformer {

    private static final Logger LOG = getLogger( GeotoolsRasterTransformer.class );

    private final Envelope sourceEnvelope;

    private final Envelope targetEnvelope;

    /**
     * GeotoolsRasterTransformer transforms an image from sourceEnvelope to targetEnvelope.
     *
     * @param sourceEnvelope
     *            never <code>null</code>
     * @param targetEnvelope
     *            never <code>null</code>
     */
    public GeotoolsRasterTransformer( Envelope sourceEnvelope, Envelope targetEnvelope ) {
        this.sourceEnvelope = sourceEnvelope;
        this.targetEnvelope = targetEnvelope;
    }

    /**
     * Transforms an image.
     *
     * @param image
     *            never <code>null</code>
     * @return transformed image
     */
    public BufferedImage transform( BufferedImage image ) {
        try {
            Envelope2D gtSourceEnvelope = createGtEnvelope( sourceEnvelope );
            Envelope2D gtTargetEnvelope = createGtEnvelope( targetEnvelope );
            Coverage transformedCoverage = transformCoverage( image, gtSourceEnvelope, gtTargetEnvelope );
            return createImage( transformedCoverage );
        } catch ( NoSuchElementException e ) {
            handleTransformException( e );
            return image;
        } catch ( FactoryException e ) {
            handleTransformException( e );
            return image;
        }
    }

    private Envelope2D createGtEnvelope( Envelope envelope )
                            throws FactoryException {
        try {
            String epsgCode = retrieveEpsgCode( envelope );
            CoordinateReferenceSystem crs = CRS.decode( epsgCode );
            double minX = envelope.getMin().get0();
            double minY = envelope.getMin().get1();
            double width = envelope.getMax().get0() - minX;
            double height = envelope.getMax().get1() - minY;
            return new Envelope2D( crs, minX, minY, width, height );
        } catch ( NoSuchElementException e ) {
            LOG.warn( "Element could not be found: " + e.getMessage() );
            e.printStackTrace();
            throw e;
        } catch ( FactoryException e ) {
            LOG.warn( "Geotools CRS could not be created: " + e.getMessage() );
            e.printStackTrace();
            throw e;
        }
    }

    private String retrieveEpsgCode( Envelope envelope ) {
        CRSCodeType[] codes = envelope.getCoordinateSystem().getCodes();
        for ( int i = 0; i < codes.length; i++ ) {
            CRSCodeType code = codes[i];
            if ( code.getCodeSpace().equals( "epsg" ) ) {
                return "epsg:" + code.getCode();
            }
        }
        throw new NoSuchElementException( "No epsg code could be found!" );
    }

    private Coverage transformCoverage( BufferedImage image, Envelope2D gtSourceEnvelope, Envelope2D gtTargetEnvelope ) {
        GridCoverageFactory coverageFactory = CoverageFactoryFinder.getGridCoverageFactory( null );
        GridCoverage2D coverage = coverageFactory.create( "coverageToTransform", image, gtSourceEnvelope );
        return DEFAULT.resample( coverage, gtTargetEnvelope, getInstance( INTERP_BILINEAR ) );
    }

    private BufferedImage createImage( Coverage transformedCoverage ) {
        RenderedImage renderedImage = ( (GridCoverage2D) transformedCoverage ).getRenderedImage();
        NullOpImage opImage = new NullOpImage( renderedImage, null, OP_IO_BOUND, null );
        return opImage.getAsBufferedImage();
    }

    private void handleTransformException( Exception e ) {
        LOG.warn( "Geotools transformation is canceled as geotools envelopes could not be created!" + e.getMessage() );
    }

}