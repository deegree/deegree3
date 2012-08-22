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
package org.deegree.model.coverage.grid;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.CodeList;
import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.ConvenienceFileFilter;
import org.deegree.framework.util.StringTools;
import org.deegree.io.shpapi.ShapeFile;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.Feature;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.wcs.configuration.Directory;
import org.deegree.ogcwebservices.wcs.configuration.Extension;
import org.deegree.ogcwebservices.wcs.configuration.File;
import org.deegree.ogcwebservices.wcs.configuration.GridDirectory;
import org.deegree.ogcwebservices.wcs.configuration.Shape;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageOffering;

/**
 * Support for creation of grid coverages from persistent formats as well as exporting a grid
 * coverage to a persistent formats. For example, it allows for creation of grid coverages from the
 * GeoTIFF Well-known binary format and exporting to the GeoTIFF file format. Basic implementations
 * only require creation of grid coverages from a file format or resource. More sophesticated
 * implementations may extract the grid coverages from a database. In such case, a
 * <code>GridCoverageExchange</code> instance will hold a connection to a specific database and
 * the dispose method will need to be invoked in order to close this connection.
 * <p>
 *
 * @author Andreas Poth
 * @version 1.0
 * @since 2.0
 */
public class GridCoverageExchange {

    private static final ILogger LOG = LoggerFactory.getLogger( GridCoverageExchange.class );

    private static final URI DEEGREEAPP = CommonNamespaces.buildNSURI( "http://www.deegree.org/app" );

    private static final String APP_PREFIX = "app";

    /**
     *
     */
    public static final String SHAPE_IMAGE_FILENAME = "FILENAME";

    /**
     *
     */
    public static final String SHAPE_DIR_NAME = "FOLDER";

    private CoverageOffering coverageOffering;

    /**
     * @param coverageOffering
     */
    public GridCoverageExchange( CoverageOffering coverageOffering ) {
        this.coverageOffering = coverageOffering;
    }

    /**
     * Returns a grid coverage reader that can manage the specified source
     *
     * @param source
     *            An object that specifies somehow the data source. Can be a
     *            {@link java.lang.String}, an {@link java.io.InputStream}, a
     *            {@link java.nio.channels.FileChannel}, whatever. It's up to the associated grid
     *            coverage reader to make meaningful use of it.
     * @return The grid coverage reader.
     * @throws IOException
     *             if an error occurs during reading.
     *
     * @revisit We need a mechanism to allow the right GridCoverageReader Something like an SPI.
     *          What if we can't find a GridCoverageReader? Do we return null or throw an Exception?
     */
    public GridCoverageReader getReader( Object source )
                            throws IOException {
        if ( !( source instanceof InputStream ) ) {
            throw new IOException( "source parameter must be an instance of InputStream" );
        }
        return null;
    }

    /**
     * This method is a deegree specific enhancement of the <tt>GridCoverageExchange</tt>
     * class/interface as defined by GeoAPI. Returns a grid coverage reader that can manage the
     * specified source
     *
     * @param source
     *            An object that specifies somehow the data source.
     * @param description
     *            an object describing the grid coverage and the access to avaiable metadata
     * @param envelope
     * @param format
     * @return The grid coverage reader.
     * @throws IOException
     *             if an error occurs during reading.
     *
     * @revisit We need a mechanism to allow the right GridCoverageReader Something like an SPI.
     *          What if we can't find a GridCoverageReader? Do we return null or throw an Exception?
     */
    public GridCoverageReader getReader( InputStream source, CoverageOffering description, Envelope envelope,
                                         Format format )
                            throws IOException {
        GridCoverageReader gcr = null;
        Extension ext = description.getExtension();
        String type = ext.getType();
        if ( type.equals( Extension.FILEBASED ) || type.equals( Extension.SCRIPTBASED ) ) {
            if ( format.getName().toUpperCase().indexOf( "GEOTIFF" ) > -1 ) {
                gcr = new GeoTIFFGridCoverageReader( source, description, envelope, format );
            } else if ( isImageFormat( format ) ) {
                gcr = new ImageGridCoverageReader( source, description, envelope, format );
            } else {
                throw new IOException( "not supported file format: " + format.getName() );
            }
        } else {
            throw new IOException( "coverage storage type: " + type
                                   + " is not supported with method: getReader(InputStream, "
                                   + "CoverageOffering, Envelope, Format )" );
        }
        return gcr;
    }

    /**
     * This method is a deegree specific enhancement of the <tt>GridCoverageExchange</tt>
     * class/interface as defined by GeoAPI. Returns a grid coverage reader that can manage the
     * specified source
     *
     * @param resource
     *            a string that specifies somehow the data source (e.g. a file).
     * @param description
     *            an object describing the grid coverage and the access to avaiable metadata
     * @param envelope
     * @param format
     *
     * @return The grid coverage reader.
     * @throws IOException
     *             if an error occurs during reading.
     * @throws InvalidParameterValueException
     *
     * @revisit We need a mechanism to allow the right GridCoverageReader Something like an SPI.
     *          What if we can't find a GridCoverageReader? Do we return null or throw an Exception?
     */
    public GridCoverageReader getReader( Object resource, CoverageOffering description, Envelope envelope, Format format )
                            throws IOException, InvalidParameterValueException {
        GridCoverageReader gcr = null;
        Extension ext = description.getExtension();
        String type = ext.getType();
        if ( type.equals( Extension.FILEBASED ) || type.equals( Extension.SCRIPTBASED )) {
            File file = new File( null, (String) resource, envelope );
            if ( format.getName().toUpperCase().indexOf( "GEOTIFF" ) > -1 ) {
                LOG.logDebug( "creating GeoTIFFGridCoverageReader" );
                gcr = new GeoTIFFGridCoverageReader( file, description, envelope, format );
            } else if ( isImageFormat( format ) ) {
                LOG.logDebug( "creating ImageGridCoverageReader" );
                gcr = new ImageGridCoverageReader( file, description, envelope, format );
            } else {
                throw new IOException( "not supported file format: " + format.getName() );
            }
        } else if ( type.equals( Extension.NAMEINDEXED ) ) {
            LOG.logDebug( "creating nameIndexed CompoundGridCoverageReader" );
            Directory[] dirs = new Directory[] { (Directory) resource };
            gcr = getReader( dirs, description, envelope, format );
        } else if ( type.equals( Extension.SHAPEINDEXED ) ) {
            LOG.logDebug( "creating shapeIndexed CompoundGridCoverageReader" );
            File[] files = null;
            try {
                files = getFilesFromShape( (Shape) resource, envelope, description );
            } catch ( UnknownCRSException e ) {
                throw new InvalidParameterValueException( e );
            }
            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                for ( int i = 0; i < files.length; i++ ) {
                    LOG.logDebug( "matching tile: ", files[i].getName() );
                }
            }
            gcr = getReader( files, description, envelope, format );
        } else if ( type.equals( Extension.ORACLEGEORASTER ) ) {
            LOG.logDebug( "creating OracleGeoRasterGridCoverageReader" );
            Class<?> clzz;
            try {
                clzz = Class.forName( "org.deegree.model.coverage.grid.oracle.GeoRasterReaderAccess" );
            } catch ( ClassNotFoundException e ) {
                LOG.logError( e.getMessage(), e );
                throw new InvalidParameterValueException( e );
            }
            GCReaderAccess acc;
            try {
                acc = (GCReaderAccess) clzz.newInstance();
            } catch ( InstantiationException e ) {
                LOG.logError( e.getMessage(), e );
                throw new InvalidParameterValueException( e );
            } catch ( IllegalAccessException e ) {
                LOG.logError( e.getMessage(), e );
                throw new InvalidParameterValueException( e );
            }
            gcr = acc.createGridCoverageReader( resource, description, envelope, format );
        } else if ( type.equals( Extension.DATABASEINDEXED ) ) {
            LOG.logDebug( "creating databaseIndexed CompoundGridCoverageReader" );
            File[] files = null;
            try {
                files = getFilesFromDatabase( (DatabaseIndexedGCMetadata) resource, envelope, description );
            } catch ( UnknownCRSException e ) {
                LOG.logError( e.getMessage(), e );
                throw new InvalidParameterValueException( e );
            }
            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                for ( int i = 0; i < files.length; i++ ) {
                    LOG.logDebug( "matching tile: ", files[i].getName() );
                }
            }
            gcr = new CompoundGridCoverageReader( files, description, envelope, format );
        } else {
            throw new IOException( "coverage storage type: " + type + " is not supported" );
        }
        return gcr;
    }

    /**
     * reads the names of the grid coverage files intersecting the requested region from the passed
     * database.
     *
     * @param dbigcmd
     * @param envelope
     * @param description
     * @return file list
     * @throws UnknownCRSException
     * @throws InvalidParameterValueException
     */
    private File[] getFilesFromDatabase( DatabaseIndexedGCMetadata dbigcmd, Envelope envelope,
                                         CoverageOffering description )
                            throws UnknownCRSException, InvalidParameterValueException {

        CoordinateSystem crs = createNativeCRS( description );

        String className = null;
        if ( dbigcmd.getJDBCConnection().getDriver().toUpperCase().indexOf( "ORACLE" ) > -1 ) {
            className = DatabaseIndexAccessMessages.getString( "oracle" );
        } else if ( dbigcmd.getJDBCConnection().getDriver().toUpperCase().indexOf( "POSTGRES" ) > -1 ) {
            className = DatabaseIndexAccessMessages.getString( "postgres" );
        }
        Class<?> clzz;
        try {
            clzz = Class.forName( className );
        } catch ( ClassNotFoundException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException( className, e );
        }
        DatabaseIndexAccess dbia;
        try {
            dbia = (DatabaseIndexAccess) clzz.newInstance();
        } catch ( InstantiationException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException( className, e );
        } catch ( IllegalAccessException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException( className, e );
        }

        return dbia.getFiles( dbigcmd, envelope, crs );
    }

    /**
     * This method is a deegree specific enhancement of the <tt>GridCoverageExchange</tt>
     * class/interface as defined by GeoAPI. Returns a grid coverage reader that can manage the
     * specified source
     *
     * @param resources
     *            an array strings that specifies somehow the data sources (e.g. some files).
     * @param description
     *            an object describing the grid coverage and the access to avaiable metadata
     * @param envelope
     * @param format
     * @return The grid coverage reader.
     * @throws IOException
     *             if an error occurs during reading.
     * @throws InvalidParameterValueException
     *
     * @revisit We need a mechanism to allow the right GridCoverageReader Something like an SPI.
     *          What if we can't find a GridCoverageReader? Do we return null or throw an Exception?
     */
    public GridCoverageReader getReader( Object[] resources, CoverageOffering description, Envelope envelope,
                                         Format format )
                            throws IOException, InvalidParameterValueException {

        // CS_CoordinateSystem crs = createNativeCRS( description );
        GridCoverageReader gcr = null;
        Extension ext = description.getExtension();
        String type = ext.getType();
        File[] files = null;
        if ( type.equals( Extension.FILEBASED ) ||  type.equals( Extension.SCRIPTBASED ) ) {
            LOG.logDebug( "creating filebased CompoundGridCoverageReader" );
            files = (File[]) resources;
            gcr = new CompoundGridCoverageReader( files, description, envelope, format );
        } else if ( type.equals( Extension.NAMEINDEXED ) ) {
            LOG.logDebug( "creating nameIndexed CompoundGridCoverageReader" );
            try {
                files = getFilesFromDirectories( (Directory[]) resources, envelope, description );
            } catch ( UnknownCRSException e ) {
                throw new InvalidParameterValueException( e );
            }
            gcr = new CompoundGridCoverageReader( files, description, envelope, format );
        } else if ( type.equals( Extension.SHAPEINDEXED ) ) {
            LOG.logDebug( "creating shapeIndexed CompoundGridCoverageReader" );
            files = (File[]) resources;
            gcr = new CompoundGridCoverageReader( files, description, envelope, format );
        } else if ( type.equals( Extension.ORACLEGEORASTER ) ) {
            LOG.logDebug( "creating OracleGeoRasterGridCoverageReader" );
            Class<?> clzz;
            try {
                clzz = Class.forName( "org.deegree.model.coverage.grid.oracle.GeoRasterReaderAccess" );
            } catch ( ClassNotFoundException e ) {
                LOG.logError( e.getMessage(), e );
                throw new InvalidParameterValueException( e );
            }
            GCReaderAccess acc;
            try {
                acc = (GCReaderAccess) clzz.newInstance();
            } catch ( InstantiationException e ) {
                LOG.logError( e.getMessage(), e );
                throw new InvalidParameterValueException( e );
            } catch ( IllegalAccessException e ) {
                LOG.logError( e.getMessage(), e );
                throw new InvalidParameterValueException( e );
            }
            gcr = acc.createGridCoverageReader( resources[0], description, envelope, format );
        } else {
            throw new IOException( "coverage storage type: " + type + " is not supported" );
        }

        return gcr;
    }

    /**
     * returns true if the passed format is an image format
     *
     * @param format
     * @return <code>true</code> if the passed format is an image format
     */
    private boolean isImageFormat( Format format ) {
        String frmt = format.getName().toUpperCase();
        return frmt.equalsIgnoreCase( "png" ) || frmt.equalsIgnoreCase( "bmp" ) || frmt.equalsIgnoreCase( "tif" )
               || frmt.equalsIgnoreCase( "tiff" ) || frmt.equalsIgnoreCase( "gif" ) || frmt.equalsIgnoreCase( "jpg" )
               || frmt.equalsIgnoreCase( "jpeg" ) || frmt.indexOf( "ECW" ) > -1;
    }

    /**
     * reads the names of the grid coverage files intersecting the requested region from the passed
     * shape (name).
     *
     * @param shape
     * @param envelope
     *            requested envelope
     * @param description
     *            description (metadata) of the source coverage
     * @return file list
     * @throws IOException
     * @throws UnknownCRSException
     */
    private File[] getFilesFromShape( Shape shape, Envelope envelope, CoverageOffering description )
                            throws IOException, UnknownCRSException {

        CoordinateSystem crs = createNativeCRS( description );

        String shapeBaseName = StringTools.replace( shape.getRootFileName(), "\\", "/", true );
        String shapeDir = shapeBaseName.substring( 0, shapeBaseName.lastIndexOf( "/" ) + 1 );

        ShapeFile shp = new ShapeFile( shapeBaseName );
        File[] files = null;
        int[] idx = shp.getGeoNumbersByRect( envelope );
        if ( idx != null ) {
            files = new File[idx.length];
            try {
                for ( int i = 0; i < files.length; i++ ) {
                    Feature feature = shp.getFeatureByRecNo( idx[i] );
                    QualifiedName qn = new QualifiedName( APP_PREFIX, SHAPE_IMAGE_FILENAME, DEEGREEAPP );
                    String img = (String) feature.getDefaultProperty( qn ).getValue();
                    qn = new QualifiedName( APP_PREFIX, SHAPE_DIR_NAME, DEEGREEAPP );
                    String dir = (String) feature.getDefaultProperty( qn ).getValue();
                    if ( !( new java.io.File( dir ).isAbsolute() ) ) {
                        // solve relative path; it is assumed that the tile directories
                        // are located in the same directory as the shape file
                        dir = shapeDir + dir;
                    }
                    Geometry geom = feature.getGeometryPropertyValues()[0];
                    Envelope env = geom.getEnvelope();
                    env = GeometryFactory.createEnvelope( env.getMin(), env.getMax(), crs );
                    files[i] = new File( crs, dir.concat( "/".concat( img ) ), env );
                }
            } catch ( Exception e ) {
                shp.close();
                LOG.logError( e.getMessage(), e );
                throw new IOException( e.getMessage() );
            }
        } else {
            files = new File[0];
        }
        shp.close();

        return files;

    }

    /**
     * reads the names of the grid coverage files intersecting the requested region from raster data
     * files contained in the passed directories
     *
     * @param directories
     *            list of directories searched for matching raster files
     * @param envelope
     *            requested envelope
     * @param description
     *            description (metadata) of the source coverage
     * @return list of files intersecting the requested envelope
     * @throws UnknownCRSException
     */
    private File[] getFilesFromDirectories( Directory[] directories, Envelope envelope, CoverageOffering description )
                            throws UnknownCRSException {

        CoordinateSystem crs = createNativeCRS( description );

        List<File> list = new ArrayList<File>( 1000 );

        for ( int i = 0; i < directories.length; i++ ) {

            double widthCRS = ( (GridDirectory) directories[i] ).getTileWidth();
            double heightCRS = ( (GridDirectory) directories[i] ).getTileHeight();
            String[] extensions = directories[i].getFileExtensions();
            String dirName = directories[i].getName();

            ConvenienceFileFilter fileFilter = new ConvenienceFileFilter( false, extensions );
            java.io.File iofile = new java.io.File( dirName );
            String[] tiles = iofile.list( fileFilter );
            for ( int j = 0; j < tiles.length; j++ ) {
                int pos1 = tiles[j].indexOf( '_' );
                int pos2 = tiles[j].lastIndexOf( '.' );
                String tmp = tiles[j].substring( 0, pos1 );
                double x1 = Double.parseDouble( tmp ) / 1000d;
                tmp = tiles[j].substring( pos1 + 1, pos2 );
                double y1 = Double.parseDouble( tmp ) / 1000d;
                Envelope env = GeometryFactory.createEnvelope( x1, y1, x1 + widthCRS, y1 + heightCRS, crs );
                if ( env.intersects( envelope ) ) {
                    File file = new File( crs, dirName + '/' + tiles[j], env );
                    list.add( file );
                }
            }

        }

        File[] files = list.toArray( new File[list.size()] );

        return files;
    }

    /**
     * creates an instance of <tt>CS_CoordinateSystem</tt> from the name of the native CRS of the
     * grid coverage
     *
     * @param description
     * @return the crs
     * @throws UnknownCRSException
     */
    private CoordinateSystem createNativeCRS( CoverageOffering description )
                            throws UnknownCRSException {
        String srs = description.getSupportedCRSs().getNativeSRSs()[0].getCodes()[0];

        return CRSFactory.create( srs );
    }

    /**
     * Returns a GridCoverageWriter that can write the specified format. The file format name is
     * determined from the {@link Format} interface. Sample file formats include:
     *
     * <blockquote><table>
     * <tr>
     * <td>"GeoTIFF"</td>
     * <td>&nbsp;&nbsp;- GeoTIFF</td>
     * </tr>
     * <tr>
     * <td>"PIX"</td>
     * <td>&nbsp;&nbsp;- PCI Geomatics PIX</td>
     * </tr>
     * <tr>
     * <td>"HDF-EOS"</td>
     * <td>&nbsp;&nbsp;- NASA HDF-EOS</td>
     * </tr>
     * <tr>
     * <td>"NITF"</td>
     * <td>&nbsp;&nbsp;- National Image Transfer Format</td>
     * </tr>
     * <tr>
     * <td>"STDS-DEM"</td>
     * <td>&nbsp;&nbsp;- Standard Transfer Data Standard</td>
     * </tr>
     * </table></blockquote>
     *
     * @param destination
     *            An object that specifies somehow the data destination. Can be a
     *            {@link java.lang.String}, an {@link java.io.OutputStream}, a
     *            {@link java.nio.channels.FileChannel}, whatever. It's up to the associated grid
     *            coverage writer to make meaningful use of it.
     * @param format
     *            the output format.
     * @return The grid coverage writer.
     * @throws IOException
     *             if an error occurs during reading.
     */
    public GridCoverageWriter getWriter( Object destination, Format format )
                            throws IOException {

        LOG.logDebug( "requested format: ", format.getName() );

        GridCoverageWriter gcw = null;

        if ( !isKnownFormat( format ) ) {
            throw new IOException( "not supported Format: " + format );
        }

        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put( "offset", coverageOffering.getExtension().getOffset() );
        metadata.put( "scaleFactor", coverageOffering.getExtension().getScaleFactor() );
        if ( format.getName().equalsIgnoreCase( "GEOTIFF" ) ) {
            gcw = new GeoTIFFGridCoverageWriter( destination, metadata, null, null, format );
        } else if ( isImageFormat( format ) ) {
            gcw = new ImageGridCoverageWriter( destination, metadata, null, null, format );
        } else if ( format.getName().equalsIgnoreCase( "GML" ) || format.getName().equalsIgnoreCase( "GML2" )
                    || format.getName().equalsIgnoreCase( "GML3" ) ) {
            gcw = new GMLGridCoverageWriter( destination, metadata, null, null, format );
        } else if ( format.getName().equalsIgnoreCase( "XYZ" ) ) {
            gcw = new XYZGridCoverageWriter( destination, metadata, null, null, format );
        } else {
            throw new IOException( "not supported Format: " + format );
        }

        return gcw;
    }

    /**
     * validates if a passed format is known to an instance of <tt>GridCoverageExchange</tt>
     *
     * @param format
     * @return <code>true</code> if the format is known, <code>false</code> otherwise.
     */
    private boolean isKnownFormat( Format format ) {
        CodeList[] codeList = coverageOffering.getSupportedFormats().getFormats();
        for ( int i = 0; i < codeList.length; i++ ) {
            String[] codes = codeList[i].getCodes();
            for ( int j = 0; j < codes.length; j++ ) {
                if ( format.getName().equalsIgnoreCase( codes[j] ) ) {
                    return true;
                }
            }
        }
        LOG.logDebug( format.getName() + " not supported" );
        return false;
    }

}
