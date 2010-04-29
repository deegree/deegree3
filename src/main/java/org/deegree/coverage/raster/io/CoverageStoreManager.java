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
package org.deegree.coverage.raster.io;

import static org.deegree.coverage.raster.utils.RasterBuilder.buildTiledRaster;
import static org.deegree.coverage.raster.utils.RasterFactory.loadRasterFromFile;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.deegree.commons.datasource.configuration.AbstractGeospatialDataSourceType;
import org.deegree.commons.datasource.configuration.MultiResolutionDataSource;
import org.deegree.commons.datasource.configuration.RasterDataSource;
import org.deegree.commons.datasource.configuration.RasterFileSetType;
import org.deegree.commons.datasource.configuration.RasterFileType;
import org.deegree.commons.datasource.configuration.MultiResolutionDataSource.Resolution;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.coverage.AbstractCoverage;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.MultiResolutionRaster;
import org.deegree.cs.CRS;
import org.slf4j.Logger;

/**
 * <code>DataSourceHandler</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CoverageStoreManager {

    private static final Logger LOG = getLogger( CoverageStoreManager.class );

    private static HashMap<String, AbstractCoverage> idToCs = new HashMap<String, AbstractCoverage>();

    /**
     * @param csDir
     */
    public static void init( File csDir ) {
        File[] csConfigFiles = csDir.listFiles( new FilenameFilter() {
            @Override
            public boolean accept( File dir, String name ) {
                return name.toLowerCase().endsWith( ".xml" );
            }
        } );
        for ( File csConfigFile : csConfigFiles ) {
            String fileName = csConfigFile.getName();
            // 4 is the length of ".xml"
            String csId = fileName.substring( 0, fileName.length() - 4 );
            LOG.info( "Setting up coverage store '" + csId + "' from file '" + fileName + "'..." + "" );

            try {
                AbstractCoverage cs = create( csConfigFile.toURI().toURL() );
                if ( cs != null ) {
                    idToCs.put( csId, cs );
                }
            } catch ( Exception e ) {
                LOG.error( "Error initializing feature store: " + e.getMessage(), e );
            }
        }
    }

    /**
     * @param url
     * @return null, if an error occurred
     */
    public static AbstractCoverage create( URL url ) {
        try {
            JAXBContext jc = JAXBContext.newInstance( "org.deegree.commons.datasource.configuration" );
            Unmarshaller u = jc.createUnmarshaller();
            Object config = u.unmarshal( url );

            XMLAdapter resolver = new XMLAdapter();
            resolver.setSystemId( url.toString() );

            if ( config instanceof MultiResolutionDataSource ) {
                return fromDatasource( (MultiResolutionDataSource) config, resolver );
            }
            if ( config instanceof RasterDataSource ) {
                return fromDatasource( (RasterDataSource) config, resolver );
            }
            LOG.warn( "An unknown object '{}' came out of JAXB parsing. This is probably a bug.", config.getClass() );
        } catch ( JAXBException e ) {
            LOG.warn( "Coverage datastore configuration from '{}' could not be read: '{}'.", url,
                      e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }
        return null;
    }

    /**
     * @param id
     * @return null, if not found
     */
    public static AbstractCoverage get( String id ) {
        return idToCs.get( id );
    }

    /**
     * @return all coverages
     */
    public static Collection<AbstractCoverage> getAll() {
        return idToCs.values();
    }

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
                    String fp = directory.getFilePattern();
                    if ( fp == null ) {
                        fp = "*";
                    }
                    if ( datasource.getOriginLocation() != null ) {
                        options.add( RasterIOOptions.GEO_ORIGIN_LOCATION,
                                     datasource.getOriginLocation().toString().toUpperCase() );
                    }
                    options.add( RasterIOOptions.OPT_FORMAT, fp );
                    if ( crs != null ) {
                        options.add( RasterIOOptions.CRS, crs.getName() );
                    }
                    AbstractRaster rasterLevel = buildTiledRaster( resolutionDir, directory.isRecursive(), options );
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
                RasterIOOptions options = new RasterIOOptions();
                if ( datasource.getOriginLocation() != null ) {
                    options.add( RasterIOOptions.GEO_ORIGIN_LOCATION,
                                 datasource.getOriginLocation().toString().toUpperCase() );
                }
                if ( directory != null ) {
                    File rasterFiles = new File( adapter.resolve( directory.getValue() ).getFile() );
                    boolean recursive = directory.isRecursive() == null ? false : directory.isRecursive();
                    String fp = directory.getFilePattern();
                    if ( fp == null ) {
                        fp = "*";
                    }
                    options.add( RasterIOOptions.OPT_FORMAT, fp );
                    if ( crs != null ) {
                        options.add( RasterIOOptions.CRS, crs.getName() );
                    }
                    return buildTiledRaster( rasterFiles, recursive, options );
                }
                if ( file != null ) {
                    final File loc = new File( adapter.resolve( file.getValue() ).getFile() );
                    options.add( RasterIOOptions.OPT_FORMAT, file.getFileType() );
                    AbstractRaster raster = loadRasterFromFile( loc, options );
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
