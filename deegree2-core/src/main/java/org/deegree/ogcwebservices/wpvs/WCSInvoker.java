//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
----------------------------------------------------------------------------*/

package org.deegree.ogcwebservices.wpvs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.operator.TransposeDescriptor;

import org.deegree.datatypes.Code;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.IDGenerator;
import org.deegree.i18n.Messages;
import org.deegree.model.coverage.grid.ImageGridCoverage;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Position;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wcs.WCSException;
import org.deegree.ogcwebservices.wcs.WCService;
import org.deegree.ogcwebservices.wcs.getcoverage.DomainSubset;
import org.deegree.ogcwebservices.wcs.getcoverage.GetCoverage;
import org.deegree.ogcwebservices.wcs.getcoverage.Output;
import org.deegree.ogcwebservices.wcs.getcoverage.ResultCoverage;
import org.deegree.ogcwebservices.wcs.getcoverage.SpatialSubset;
import org.deegree.ogcwebservices.wpvs.configuration.AbstractDataSource;
import org.deegree.ogcwebservices.wpvs.configuration.LocalWCSDataSource;
import org.deegree.ogcwebservices.wpvs.utils.ImageUtils;
import org.deegree.ogcwebservices.wpvs.utils.ResolutionStripe;

/**
 * Invoker for a Web Coverage Service.
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * $Revision$, $Date$
 */
public class WCSInvoker extends GetViewServiceInvoker {

    private static final ILogger LOG = LoggerFactory.getLogger( WCSInvoker.class );

    /* the rank represent the order in whoch the image will be painted */
    private int id;

    /* whether the image will be used as texture or as data for the elevation model */
    private final boolean isElevationModelRequest;

    private String requestFormat;

    /**
     * Creates a new instance of this class.
     *
     * @param owner
     *            the handler that owns this invoker
     * @param id
     * @param requestFormat
     * @param isElevationModelRequest
     */
    public WCSInvoker( ResolutionStripe owner, int id, String requestFormat, boolean isElevationModelRequest ) {
        super( owner );
        this.id = id;
        this.isElevationModelRequest = isElevationModelRequest;
        this.requestFormat = requestFormat;
    }

    @Override
    public void invokeService( AbstractDataSource dataSource ) {

        if ( !( dataSource instanceof LocalWCSDataSource ) ) {
            LOG.logError( "The given AbstractDataSource is no WCSDataSource instance. It is needed for a WCSInvoker" );
            throw new RuntimeException( "DataSource should be a WCS-instance for a WCSInvoker" );
        }

        WCService service = null;
        try {
            service = (WCService) dataSource.getOGCWebService();
        } catch ( OGCWebServiceException ogcwe ) {
            LOG.logError( ogcwe.getMessage() );
            throw new RuntimeException( ogcwe );
        }
        if ( service == null ) {
            throw new RuntimeException( "No Web Coverage Service instance available for WCSInvoker" );
        }

        Object coverageResponse = null;

        // check if the admin has configured a minimal dgm resolution, if so the request and
        // response resolution for the dgm must be set.
        int requestWidth = resolutionStripe.getRequestWidthForBBox();
        int requestHeight = resolutionStripe.getRequestHeightForBBox();
        if ( isElevationModelRequest ) {
            double minRes = ( (LocalWCSDataSource) dataSource ).getConfiguredMinimalDGMResolution();
            LOG.logDebug( "configured minimalResolution: " + minRes );
            if ( minRes > 0.0000001 ) {// 0d if not set
                Envelope env = resolutionStripe.getSurface().getEnvelope();
                if ( ( env.getWidth() / requestWidth ) < minRes ) {
                    requestWidth = (int) ( env.getWidth() / minRes );
                }
                if ( ( env.getHeight() / requestHeight ) < minRes ) {
                    requestHeight = (int) ( env.getHeight() / minRes );
                }
            }
        }

        try {
            GetCoverage getCoverageRequest = createGetCoverageRequest(
                                                                       ( (LocalWCSDataSource) dataSource ).getCoverageFilterCondition(),
                                                                       ( (LocalWCSDataSource) dataSource ).getDefaultFormat(),
                                                                       requestWidth, requestHeight );
            LOG.logDebug( "WCS request:" + getCoverageRequest );
            coverageResponse = service.doService( getCoverageRequest );
        } catch ( WCSException wcse ) {
            if ( !Thread.currentThread().isInterrupted() ) {
                LOG.logError( Messages.getMessage( "WPVS_WCS_REQUEST_ERROR", "WCSException", dataSource.getName(),
                                                   wcse.getMessage() ) );
                if ( !isElevationModelRequest ) {
                    resolutionStripe.setTextureRetrievalException( dataSource.getName().getFormattedString() + id, wcse );
                }
            }
            return;
        } catch ( OGCWebServiceException ogcwse ) {
            if ( !Thread.currentThread().isInterrupted() ) {
                LOG.logError( Messages.getMessage( "WPVS_WCS_REQUEST_ERROR", "OGCWebServiceException",
                                                   dataSource.getName(), ogcwse.getMessage() ) );
                if ( !isElevationModelRequest ) {
                    resolutionStripe.setTextureRetrievalException( dataSource.getName().getFormattedString() + id,
                                                                   ogcwse );
                }
            }
            return;
        } catch ( Throwable t ) {
            if ( !Thread.currentThread().isInterrupted() ) {
                t.printStackTrace();
            }
            return;
        }
        if ( coverageResponse != null && coverageResponse instanceof ResultCoverage ) {

            LOG.logDebug( "\t -> a valid response\n" );
            ResultCoverage response = (ResultCoverage) coverageResponse;
            if ( response.getCoverage() != null && response.getCoverage() instanceof ImageGridCoverage ) {
                ImageGridCoverage igc = (ImageGridCoverage) response.getCoverage();

                BufferedImage image = igc.getAsImage( requestWidth, requestHeight );
                if ( !isElevationModelRequest ) {
                    if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                        try {
                            File f = new File( "wcs_texture_response.png" );
                            LOG.logDebug( "creating tmpfile for wcs texture response with name: " + f.toString() );
                            f.deleteOnExit();
                            ImageIO.write( image, "png", f );
                        } catch ( IOException e ) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    Color[] colors = ( (LocalWCSDataSource) dataSource ).getTransparentColors();
                    if ( colors != null && colors.length > 0 ) {
                        ImageUtils imgUtil = new ImageUtils( colors );
                        Image img = imgUtil.filterImage( image );
                        Graphics2D g2d = (Graphics2D) image.getGraphics();
                        g2d.drawImage( img, 0, 0, null );
                        g2d.dispose();
                    }

                    if ( !resolutionStripe.addTexture( dataSource.getName().getFormattedString() + id, image ) ) {
                        LOG.logDebug( "could not add the texture" );
                    }
                } else {
                    // the heightdata is in x and -y coordinates, they must be flipped before using
                    PlanarImage im2 = JAI.create( "transpose", image, TransposeDescriptor.FLIP_VERTICAL );
                    if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                        try {
                            File f = new File( "wcs_dgm_response.png" );
                            LOG.logDebug( "creating tmpfile for wcs elevationmodel response with name: " + f.toString() );
                            f.deleteOnExit();
                            ImageIO.write( im2.getAsBufferedImage(), "png", f );
                        } catch ( Exception e ) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    resolutionStripe.setElevationModelFromHeightMap( im2.getAsBufferedImage() );
                }
            } else {
                LOG.logWarning( Messages.getMessage( "WPVS_IVALID_WCS_RESPONSE", dataSource.getName(),
                                                     "an ImageGridCoverage" ) );
            }
        } else {
            LOG.logWarning( Messages.getMessage( "WPVS_IVALID_WCS_RESPONSE", dataSource.getName(), "a ResultCoverage" ) );
        }

    }

    /**
     * Creates a getCoverage request for the given surface
     *
     * @param filterCondition
     * @param format
     * @param requestWidth
     *            the width of a request (which should take the configured dgm_res into account).
     * @param requestHeight
     *            the height of a request (which should take the configured dgm_res into account).
     * @return a new GetCoverageRequest.
     * @throws WCSException
     * @throws OGCWebServiceException
     */
    private GetCoverage createGetCoverageRequest( GetCoverage filterCondition, String format, int requestWidth,
                                                  int requestHeight )
                            throws WCSException, OGCWebServiceException {

        // String format = "GeoTiff";
        if ( !isElevationModelRequest ) {
            if ( filterCondition.getOutput().getFormat() == null ) {
                // fallback if no output format has been defined for a
                // WCS datasource
                format = requestFormat;
            } else {
                format = filterCondition.getOutput().getFormat().getCode();
            }
            int pos = format.indexOf( '/' );
            if ( pos > -1 ) {
                format = format.substring( pos + 1, format.length() );
            }

            if ( format.indexOf( "svg" ) > -1 ) {
                format = "png";
            }
        }
        Output output = GetCoverage.createOutput( resolutionStripe.getCRSName().getFormattedString(), null, format,
                                                  null );

        // put mising parts in this map:
        Map<String, String> map = new HashMap<String, String>( 5 );

        StringBuffer sb = new StringBuffer( 1000 );
        Envelope env = resolutionStripe.getSurface().getEnvelope();
        Position p = env.getMin();
        sb.append( p.getX() ).append( "," ).append( p.getY() ).append( "," );
        p = env.getMax();
        sb.append( p.getX() ).append( "," ).append( p.getY() );
        map.put( "BBOX", sb.toString() );

        map.put( "WIDTH", String.valueOf( requestWidth ) );
        map.put( "HEIGHT", String.valueOf( requestHeight ) );

        SpatialSubset sps = GetCoverage.createSpatialSubset( map, resolutionStripe.getCRSName().getFormattedString() );

        Code code = filterCondition.getDomainSubset().getRequestSRS();
        DomainSubset domainSubset = new DomainSubset( code, sps, null );

        IDGenerator idg = IDGenerator.getInstance();

        GetCoverage getCoverageRequest = new GetCoverage( String.valueOf( idg.generateUniqueID() ),
                                                          filterCondition.getVersion(),
                                                          filterCondition.getSourceCoverage(), domainSubset, null,
                                                          filterCondition.getInterpolationMethod(), output );

        return getCoverageRequest;
    }

}
