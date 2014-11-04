package org.deegree.rendering.r2d;

import static javax.media.jai.Interpolation.INTERP_BILINEAR;
import static javax.media.jai.Interpolation.getInstance;
import static javax.media.jai.OpImage.OP_IO_BOUND;
import static org.deegree.cs.CRSUtils.getEpsgCode;
import static org.deegree.cs.CRSUtils.hasEpsgCode;
import static org.geotools.coverage.processing.Operations.DEFAULT;
import static org.geotools.referencing.CRS.decode;
import static org.slf4j.LoggerFactory.getLogger;

import javax.media.jai.NullOpImage;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import org.deegree.cs.components.Axis;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.geometry.Envelope2D;
import org.opengis.coverage.Coverage;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.slf4j.Logger;

/**
 * Can be used to transform raster images. The transform mechanism uses geotools.
 *
 * @author <a href="mailto:stenger@lat-lon.de">Dirk Stenger</a>
 * @author last edited by: $Author: stenger $
 * @version $Revision: $, $Date: $
 */
public class GeotoolsRasterTransformer implements ImageTransformer {

    private static final Logger LOG = getLogger( GeotoolsRasterTransformer.class );

    private final Envelope targetEnvelope;

    /**
     * GeotoolsRasterTransformer transforms an image from sourceEnvelope to targetEnvelope.
     *
     * @param targetEnvelope
     *            target envelope of image, never <code>null</code>
     */
    public GeotoolsRasterTransformer( Envelope targetEnvelope ) {
        this.targetEnvelope = targetEnvelope;
    }

    @Override
    public BufferedImage transform( BufferedImage image, Envelope sourceEnvelope ) {
        try {
            Envelope2D gtSourceEnvelope = createGtEnvelope( sourceEnvelope );
            Envelope2D gtTargetEnvelope = createGtEnvelope( targetEnvelope );
            Coverage transformedCoverage = transformCoverage( image, gtSourceEnvelope, gtTargetEnvelope );
            return createImage( transformedCoverage );
        } catch ( FactoryException e ) {
            LOG.warn( "Geotools transformation is canceled as geotools envelopes could not be created!"
                      + e.getMessage() );
            return image;
        }
    }

    private Envelope2D createGtEnvelope( Envelope envelope )
                            throws FactoryException {
        try {
            String crsCode = retrieveCrsCode( envelope );
            CoordinateReferenceSystem crs = decode( crsCode );

            int dEnv0 = envelope.getCoordinateSystem().getAxis()[0].getOrientation();
            AxisDirection gtEnv0 = crs.getCoordinateSystem().getAxis(0).getDirection();

            boolean deegreeCrsIsYX = dEnv0 == Axis.AO_NORTH;
            boolean geotoolsCrsIsYX = gtEnv0.equals(AxisDirection.NORTH);

            double minX, minY, maxX, maxY;
            if (deegreeCrsIsYX == geotoolsCrsIsYX) {
                LOG.debug("keeping axis order for geotools raster transformation");
                minX = envelope.getMin().get0();
                minY = envelope.getMin().get1();
                maxX = envelope.getMax().get0();
                maxY = envelope.getMax().get1();
            }
            else {
                LOG.debug("inverting axis order for geotools raster transformation");
                minX = envelope.getMin().get1();
                minY = envelope.getMin().get0();
                maxX = envelope.getMax().get1();
                maxY = envelope.getMax().get0();
            }
            double width = maxX - minX;
            double height = maxY - minY;

            return new Envelope2D( crs, minX, minY, width, height );
        } catch ( FactoryException e ) {
            LOG.warn( "Geotools CRS could not be created: " + e.getMessage() );
            e.printStackTrace();
            throw e;
        }
    }

    private String retrieveCrsCode( Envelope envelope ) {
        ICRS crs = envelope.getCoordinateSystem();
        if ( hasEpsgCode( crs ) ) {
            LOG.debug( "CRS" + crs.getAlias() + "has an epsg code." );
            return retrieveEpsgCode( crs );
        } else {
            LOG.debug( "CRS" + crs.getAlias() + "does not have an epsg code. Identifier of CRS is used instead!" );
            return crs.getAlias();
        }

    }

    private String retrieveEpsgCode( ICRS crs ) {
        int code = getEpsgCode( crs );
        return "epsg:" + code;
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

}