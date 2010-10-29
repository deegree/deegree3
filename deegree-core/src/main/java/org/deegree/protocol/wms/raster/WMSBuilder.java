//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.protocol.wms.raster;

import static org.deegree.protocol.wms.raster.WMSReader.RIO_WMS_DEFAULT_FORMAT;
import static org.deegree.protocol.wms.raster.WMSReader.RIO_WMS_ENABLE_TRANSPARENT;
import static org.deegree.protocol.wms.raster.WMSReader.RIO_WMS_LAYERS;
import static org.deegree.protocol.wms.raster.WMSReader.RIO_WMS_MAX_HEIGHT;
import static org.deegree.protocol.wms.raster.WMSReader.RIO_WMS_MAX_SCALE;
import static org.deegree.protocol.wms.raster.WMSReader.RIO_WMS_MAX_WIDTH;
import static org.deegree.protocol.wms.raster.WMSReader.RIO_WMS_REFRESH_TIME;
import static org.deegree.protocol.wms.raster.WMSReader.RIO_WMS_SYS_ID;
import static org.deegree.protocol.wms.raster.WMSReader.RIO_WMS_TIMEOUT;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.deegree.commons.utils.ProxyUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.coverage.AbstractCoverage;
import org.deegree.coverage.persistence.CoverageBuilder;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.MultiResolutionRaster;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.cs.CRS;
import org.deegree.protocol.wms.raster.jaxb.MultiResolutionRasterConfig;
import org.deegree.protocol.wms.raster.jaxb.WMSDataSourceType;
import org.deegree.protocol.wms.raster.jaxb.MultiResolutionRasterConfig.Resolution;
import org.deegree.protocol.wms.raster.jaxb.WMSDataSourceType.CapabilitiesDocumentLocation;
import org.deegree.protocol.wms.raster.jaxb.WMSDataSourceType.MaxMapDimensions;
import org.deegree.protocol.wms.raster.jaxb.WMSDataSourceType.RequestedFormat;
import org.slf4j.Logger;

/**
 * Builds a wms backed coverage
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WMSBuilder implements CoverageBuilder {

    private static final Logger LOG = getLogger( WMSBuilder.class );

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/datasource/coverage/wms";
    }

    @Override
    public AbstractCoverage buildCoverage( URL configURL )
                            throws IOException {
        try {
            JAXBContext jc = JAXBContext.newInstance( "org.deegree.protocol.wms.raster.jaxb" );
            Unmarshaller u = jc.createUnmarshaller();
            Object config = u.unmarshal( configURL );

            XMLAdapter resolver = new XMLAdapter();
            resolver.setSystemId( configURL.toString() );

            if ( config instanceof MultiResolutionRasterConfig ) {
                return fromJAXB( (MultiResolutionRasterConfig) config, resolver );
            }
            if ( config instanceof WMSDataSourceType ) {
                return fromJAXB( (WMSDataSourceType) config, resolver, null );
            }
            LOG.warn( "An unknown object '{}' came out of JAXB parsing. This is probably a bug.", config.getClass() );
        } catch ( JAXBException e ) {
            LOG.warn( "Coverage datastore configuration from '{}' could not be read: '{}'.", configURL,
                      e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }
        return null;
    }

    /**
     * @param mrrConfig
     * @param adapter
     * @return a corresponding raster
     */
    private MultiResolutionRaster fromJAXB( MultiResolutionRasterConfig mrrConfig, XMLAdapter adapter ) {
        if ( mrrConfig != null ) {
            String defCRS = mrrConfig.getDefaultSRS();
            // String defCRS = null;
            CRS crs = null;
            if ( defCRS != null ) {
                crs = new CRS( defCRS );
            }
            RasterIOOptions options = getOptions( mrrConfig );

            MultiResolutionRaster mrr = new MultiResolutionRaster();
            mrr.setCoordinateSystem( crs );
            for ( Resolution resolution : mrrConfig.getResolution() ) {
                if ( resolution != null ) {
                    AbstractRaster rasterLevel = null;
                    try {
                        options.add( RIO_WMS_MAX_SCALE, resolution.getRes() == null ? null
                                                                                   : resolution.getRes().toString() );
                        rasterLevel = fromJAXB( resolution, adapter, options );
                    } catch ( IOException e ) {
                        if ( LOG.isDebugEnabled() ) {
                            LOG.debug( "(Stack) Exception occurred while creating a resolution wms datasource: "
                                       + e.getLocalizedMessage(), e );
                        } else {
                            LOG.error( "Exception occurred while creating a resolution wms datasource: "
                                       + e.getLocalizedMessage() );
                        }
                    }
                    if ( rasterLevel != null ) {
                        mrr.addRaster( rasterLevel );
                    }
                }
            }
            return mrr;
        }
        throw new NullPointerException( "The configured multi resolution raster may not be null." );
    }

    private RasterIOOptions getOptions( MultiResolutionRasterConfig config ) {
        RasterIOOptions options = new RasterIOOptions();
        if ( config.getDefaultSRS() != null ) {
            options.add( RasterIOOptions.CRS, config.getDefaultSRS() );
        }
        return options;
    }

    /**
     * @param config
     * @param adapter
     * @return a corresponding raster, null if files could not be fund
     * @throws IOException
     */
    private AbstractRaster fromJAXB( WMSDataSourceType config, XMLAdapter adapter, RasterIOOptions opts )
                            throws IOException {
        if ( config != null ) {

            CapabilitiesDocumentLocation capDoc = config.getCapabilitiesDocumentLocation();
            RasterIOOptions options = new RasterIOOptions();
            options.copyOf( opts );

            options.add( RIO_WMS_SYS_ID, capDoc.getLocation() );
            // refresh time is not supported
            options.add( RIO_WMS_REFRESH_TIME, Integer.toString( capDoc.getRefreshTime() ) );

            MaxMapDimensions maxMapDimensions = config.getMaxMapDimensions();
            String width = "-1";
            String height = "-1";
            if ( maxMapDimensions != null ) {
                width = maxMapDimensions.getWidth() == null ? "-1" : maxMapDimensions.getWidth().toString();
                height = maxMapDimensions.getHeight() == null ? "-1" : maxMapDimensions.getWidth().toString();
            }
            /** Defines the maximum width of a GetMap request. */
            options.add( RIO_WMS_MAX_WIDTH, width );
            /** Defines the maximum height of a GetMap request. */
            options.add( RIO_WMS_MAX_HEIGHT, height );

            /** Defines the maximum height of a GetMap request. */
            options.add( RIO_WMS_LAYERS, config.getRequestedLayers() );

            /** Defines the maximum scale of a WMS. */
            String maxScale = config.getMaxScale() == null ? "1" : config.getMaxScale().toString();
            if ( options.get( RIO_WMS_MAX_SCALE ) != null ) {
                if ( config.getMaxScale() != null ) {
                    LOG.warn( "Configured WMS raster datasource defines a max scale, but the scale was set (from the multi resolution raster?) to: "
                              + options.get( RIO_WMS_MAX_SCALE ) );
                }
                maxScale = options.get( RIO_WMS_MAX_SCALE );
            }
            options.add( RIO_WMS_MAX_SCALE, maxScale );

            if ( config.getDefaultSRS() != null ) {
                options.add( RasterIOOptions.CRS, config.getDefaultSRS() );
            }

            RequestedFormat format = config.getRequestedFormat();
            if ( format != null ) {
                /** Defines the default (image) format of a get map request to a WMS. */
                options.add( RIO_WMS_DEFAULT_FORMAT, format.getValue() );

                /** Defines the key to set the GetMap retrieval to transparent. */
                options.add( RIO_WMS_ENABLE_TRANSPARENT, Boolean.toString( format.isTransparent() ) );
            }

            /** Defines the key to set the GetMap retrieval timeout. */
            String timeout = config.getRequestTimeout() == null ? "60" : config.getRequestTimeout().toString();
            options.add( RIO_WMS_TIMEOUT, timeout );

            // rb: currently only wms 1.1.1 reader support
            options.add( RasterIOOptions.OPT_FORMAT, WMSReader.WMSVersion.WMS_111.name() );

            URL url = new URL( capDoc.getLocation() );

            LOG.info( "Opening stream to capabilities:{}", capDoc.getLocation() );
            URLConnection connection = ProxyUtils.openURLConnection( url );
            connection.setConnectTimeout( Integer.parseInt( timeout ) * 1000 );
            InputStream in = connection.getInputStream();
            return RasterFactory.loadRasterFromStream( in, options );
        }
        throw new NullPointerException( "The configured raster datasource may not be null." );
    }
}
