//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.coverage.raster.utils;

import static org.deegree.coverage.raster.utils.RasterBuilder.buildTiledRaster;
import static org.deegree.coverage.raster.utils.RasterFactory.loadRasterFromFile;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.xml.bind.JAXBElement;

import org.deegree.commons.datasource.configuration.AbstractGeospatialDataSourceType;
import org.deegree.commons.datasource.configuration.MultiResolutionDataSource;
import org.deegree.commons.datasource.configuration.RasterDataSource;
import org.deegree.commons.datasource.configuration.RasterFileSetType;
import org.deegree.commons.datasource.configuration.RasterFileType;
import org.deegree.commons.datasource.configuration.MultiResolutionDataSource.Resolution;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.MultiResolutionRaster;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.crs.CRS;
import org.slf4j.Logger;

/**
 * <code>DataSourceHandler</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DataSourceHandler {

    private static final Logger LOG = getLogger( DataSourceHandler.class );

    /**
     * @param datasource
     * @param adapter
     * @return a corresponding raster
     */
    public static MultiResolutionRaster fromDatasource( MultiResolutionDataSource datasource, XMLAdapter adapter ) {
        if ( datasource != null ) {
            String defCRS = datasource.getCrs();
            CRS crs = null;
            if ( defCRS != null ) {
                crs = new CRS( defCRS );
            }
            MultiResolutionRaster mrr = new MultiResolutionRaster();
            mrr.setCoordinateSystem( crs );
            for ( Resolution resolution : datasource.getResolution() ) {
                JAXBElement<? extends AbstractGeospatialDataSourceType> dsElement = resolution.getAbstractGeospatialDataSource();
                RasterDataSource ds = (RasterDataSource) dsElement.getValue();
                RasterFileSetType directory = ds.getRasterDirectory();
                File resolutionDir;
                try {
                    resolutionDir = new File( adapter.resolve( directory.getValue() ).getFile() );
                    RasterIOOptions options = new RasterIOOptions();
                    if ( crs != null ) {
                        options.add( RasterIOOptions.CRS, crs.getName() );
                    }
                    AbstractRaster rasterLevel = buildTiledRaster( resolutionDir, directory.getFilePattern(),
                                                                   directory.isRecursive(), options );
                    // double res = RasterBuilder.getPixelResolution( resolution.getRes(), resolutionDir );
                    mrr.addRaster( rasterLevel );
                } catch ( MalformedURLException e ) {
                    LOG.warn( "Could not resolve the file {}, corresponding data will not be available.",
                              directory.getValue() );
                }
            }
            return mrr;
        }
        throw new NullPointerException( "The configured multi resolution datasource may not be null." );

    }

    /**
     * @param datasource
     * @param adapter
     * @return a corresponding raster, null if files could not be fund
     */
    public static AbstractRaster fromDatasource( RasterDataSource datasource, XMLAdapter adapter ) {
        if ( datasource != null ) {
            String defCRS = datasource.getCrs();
            CRS crs = null;
            if ( defCRS != null ) {
                crs = new CRS( defCRS );
            }
            RasterFileSetType directory = datasource.getRasterDirectory();
            RasterFileType file = datasource.getRasterFile();
            try {
                if ( directory != null ) {
                    File rasterFiles = new File( adapter.resolve( directory.getValue() ).getFile() );
                    boolean recursive = directory.isRecursive() == null ? false : directory.isRecursive();
                    RasterIOOptions options = new RasterIOOptions();
                    if ( crs != null ) {
                        options.add( RasterIOOptions.CRS, crs.getName() );
                    }
                    return buildTiledRaster( rasterFiles, directory.getFilePattern(), recursive, options );
                }
                if ( file != null ) {
                    final File loc = new File( adapter.resolve( file.getValue() ).getFile() );
                    AbstractRaster raster = loadRasterFromFile( loc );
                    raster.setCoordinateSystem( crs );
                    return raster;
                }
            } catch ( MalformedURLException e ) {
                if ( directory != null ) {
                    LOG.warn( "Could not resolve the file {}, corresponding data will not be available.",
                              directory.getValue() );
                } else {
                    LOG.warn( "Could not resolve the file {}, corresponding data will not be available.",
                              file.getValue() );
                }
            } catch ( IOException e ) {
                LOG.warn( "Could not load the file {}, corresponding data will not be available.", file.getValue() );
            }
        }
        throw new NullPointerException( "The configured raster datasource may not be null." );
    }

}
