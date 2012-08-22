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
package org.deegree.io.shpapi.shape_new;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.io.dbaseapi.DBaseFile;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.ByteUtils;

/**
 * <code>ShapeFileReader</code> is a class to read shapefiles.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ShapeFileReader {

    private static final ILogger LOG = LoggerFactory.getLogger( ShapeFileReader.class );

    private String baseName;

    private CoordinateSystem defaultCRS;

    private int shapeType;

    private ShapeEnvelope envelope;

    private int length;

    /**
     * Does not read it yet - just initializes the object.
     * 
     * @param baseName
     */
    public ShapeFileReader( String baseName ) {
        if ( baseName.toLowerCase().endsWith( ".shp" ) ) {
            baseName = baseName.substring( 0, baseName.length() - 4 );
        }
        this.baseName = baseName;
    }

    /**
     * Does not read it yet - just initializes the object.
     * 
     * @param baseName
     * @param defaultCRS
     *            CoordinateSystem for the shape file
     */
    public ShapeFileReader( String baseName, CoordinateSystem defaultCRS ) {
        this( baseName );
        this.defaultCRS = defaultCRS;
    }

    private void readHeader( InputStream in )
                            throws IOException {
        byte[] header = new byte[100];

        if ( in.read( header ) != 100 ) {
            LOG.logError( "Header is too small, unexpected things might happen!" );
            return;
        }

        int fileType = ByteUtils.readBEInt( header, 0 );
        if ( fileType != ShapeFile.FILETYPE ) {
            LOG.logWarning( "File type is wrong, unexpected things might happen, continuing anyway..." );
        }

        length = ByteUtils.readBEInt( header, 24 ) * 2; // 16 bit words...

        int version = ByteUtils.readLEInt( header, 28 );
        if ( version != ShapeFile.VERSION ) {
            LOG.logWarning( "File version is wrong, continuing in the hope of compatibility..." );
        }

        shapeType = ByteUtils.readLEInt( header, 32 );

        envelope = new ShapeEnvelope( false, false );
        envelope.read( header, 36 );

        // it shouldn't hurt to write these values as doubles default to 0.0 anyway
        double zmin = ByteUtils.readLEDouble( header, 68 );
        double zmax = ByteUtils.readLEDouble( header, 76 );
        double mmin = ByteUtils.readLEDouble( header, 84 );
        double mmax = ByteUtils.readLEDouble( header, 92 );

        switch ( shapeType ) {
        case ShapeFile.NULL:
        case ShapeFile.POINT:
        case ShapeFile.POLYLINE:
        case ShapeFile.POLYGON:
        case ShapeFile.MULTIPOINT:
            break;

        case ShapeFile.POINTM:
        case ShapeFile.POLYLINEM:
        case ShapeFile.POLYGONM:
        case ShapeFile.MULTIPOINTM:
            envelope.extend( mmin, mmax );
            break;

        case ShapeFile.POINTZ:
        case ShapeFile.POLYLINEZ:
        case ShapeFile.POLYGONZ:
        case ShapeFile.MULTIPOINTZ:
        case ShapeFile.MULTIPATCH:
            envelope.extend( zmin, zmax, mmin, mmax );

        }
    }

    private LinkedList<Shape> readShapes( InputStream in, boolean strict )
                            throws IOException {
        LinkedList<Shape> shapes = new LinkedList<Shape>();

        int offset = 0;
        byte[] bytes = new byte[length - 100];
        // read the whole file at once, this makes the "parsing" pretty fast
        in.read( bytes );

        while ( offset < bytes.length ) {
            if ( shapes.size() % 10000 == 0 ) {
                System.out.print( shapes.size() + " shapes read.\r" );
            }

            // ByteUtils.readBEInt( hdr, 0 ); // just ignore the record number
            int len = ByteUtils.readBEInt( bytes, offset + 4 ) * 2;
            offset += 8;

            if ( strict ) {
                Shape s = null;
                switch ( shapeType ) {
                case ShapeFile.NULL:
                    break;
                case ShapeFile.POINT:
                    s = new ShapePoint( false, false, defaultCRS );
                    break;
                case ShapeFile.POINTM:
                    s = new ShapePoint( false, true, defaultCRS );
                    break;
                case ShapeFile.POINTZ:
                    s = new ShapePoint( true, false, defaultCRS );
                    break;
                case ShapeFile.POLYLINE:
                    s = new ShapePolyline( false, false, defaultCRS );
                    break;
                case ShapeFile.POLYLINEM:
                    s = new ShapePolyline( false, true, defaultCRS );
                    break;
                case ShapeFile.POLYLINEZ:
                    s = new ShapePolyline( true, false, defaultCRS );
                    break;
                case ShapeFile.POLYGON:
                    s = new ShapePolygon( false, false, defaultCRS );
                    break;
                case ShapeFile.POLYGONM:
                    s = new ShapePolygon( false, true, defaultCRS );
                    break;
                case ShapeFile.POLYGONZ:
                    s = new ShapePolygon( true, false, defaultCRS );
                    break;
                case ShapeFile.MULTIPOINT:
                    s = new ShapeMultiPoint( false, false, defaultCRS );
                    break;
                case ShapeFile.MULTIPOINTM:
                    s = new ShapeMultiPoint( false, true, defaultCRS );
                    break;
                case ShapeFile.MULTIPOINTZ:
                    s = new ShapeMultiPoint( true, false, defaultCRS );
                    break;
                case ShapeFile.MULTIPATCH:
                    s = new ShapeMultiPatch( len, defaultCRS );
                    break;
                }

                LOG.logDebug( "Reading shape type " + s.getClass().getSimpleName() );

                int alen = s.read( bytes, offset ) - offset;

                if ( len != alen ) {
                    LOG.logWarning( "Length is supposedly " + len + ", actual read length was " + alen );
                    // broken files that omit the M-section and that add the record length to the
                    // length header:
                    offset += len - 8;
                } else {
                    offset += len;
                }

                shapes.add( s );

            } else {
                // TODO
            }
        }

        LOG.logInfo( "Read " + shapes.size() + " shapes in total." );

        return shapes;
    }

    /**
     * @return the shape file contents.
     * @throws IOException
     */
    public ShapeFile read()
                            throws IOException {
        File mainFile = new File( baseName + ".shp" );
        BufferedInputStream mainIn = new BufferedInputStream( new FileInputStream( mainFile ) );
        readHeader( mainIn );

        LinkedList<Shape> shapes = readShapes( mainIn, true );

        DBaseFile dbf = new DBaseFile( baseName );

        return new ShapeFile( shapes, envelope, dbf, baseName );
    }

    /**
     * @return the dbase file
     * @throws IOException
     */
    public DBaseFile getTables()
                            throws IOException {
        return new DBaseFile( baseName );
    }

    /**
     * @return the number of shapes stored in this shape file.
     * @throws IOException
     */
    public int getShapeCount()
                            throws IOException {
        File file = new File( baseName + ".shx" );
        BufferedInputStream in = new BufferedInputStream( new FileInputStream( file ) );
        readHeader( in );
        return ( length - 100 ) / 8;
    }

    /**
     * @return the type of the shape file's contents.
     * @throws IOException
     */
    public int getShapeType()
                            throws IOException {
        File file = new File( baseName + ".shx" );
        BufferedInputStream in = new BufferedInputStream( new FileInputStream( file ) );
        readHeader( in );
        return shapeType;
    }

}
