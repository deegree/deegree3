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

package org.deegree.commons.dataaccess.dbase;

import static java.lang.Double.valueOf;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MILLISECOND;
import static org.deegree.commons.utils.EncodingGuesser.guess;
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2_OR_3;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.GEOMETRY;
import static org.deegree.feature.types.property.ValueRepresentation.BOTH;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.namespace.QName;

import org.deegree.feature.GenericProperty;
import org.deegree.feature.Property;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PrimitiveType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.slf4j.Logger;

/**
 * <code>DBFReader</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DBFReader {

    private static final Logger LOG = getLogger( DBFReader.class );

    private final RandomAccessFile in;

    private final int noOfRecords, recordLength, headerLength;

    private HashMap<String, Field> fields = new HashMap<String, Field>();

    private LinkedList<String> fieldOrder = new LinkedList<String>();

    private final Charset encoding;

    private GenericFeatureType featureType;

    /**
     * Already reads/parses the header.
     * 
     * @param in
     * @param encoding
     * @param ftName
     *            the name of the feature type
     * @throws IOException
     */
    public DBFReader( RandomAccessFile in, Charset encoding, QName ftName ) throws IOException {
        this.encoding = encoding;
        this.in = in;
        int version = in.readUnsignedByte();
        if ( version < 3 || version > 5 ) {
            LOG.warn( "DBase file is of unsupported version " + version + ". Trying to continue anyway..." );
        }
        if ( LOG.isTraceEnabled() ) {
            LOG.trace( "Version number: " + version );
            int year = 1900 + in.readUnsignedByte();
            int month = in.readUnsignedByte();
            int day = in.readUnsignedByte();
            LOG.trace( "Last modified: " + year + "/" + month + "/" + day );
        } else {
            in.skipBytes( 3 );
        }

        noOfRecords = in.readUnsignedByte() + ( in.readUnsignedByte() << 8 ) + ( in.readUnsignedByte() << 16 )
                      + ( in.readUnsignedByte() << 24 );
        LOG.trace( "Number of records: " + noOfRecords );

        headerLength = in.readUnsignedByte() + ( in.readUnsignedByte() << 8 );
        LOG.trace( "Length of header: " + headerLength );

        recordLength = in.readUnsignedByte() + ( in.readUnsignedByte() << 8 );
        LOG.trace( "Record length: " + recordLength );
        in.seek( 14 );
        int dirty = in.readUnsignedByte();
        if ( dirty == 1 ) {
            LOG.warn( "DBase file is marked as 'transaction in progress'. Unexpected things may happen." );
        }
        int enc = in.readUnsignedByte();
        if ( enc == 1 ) {
            LOG.warn( "DBase file is marked as encrypted. This is unsupported, so you'll get garbage output." );
        }

        if ( LOG.isTraceEnabled() ) {
            in.seek( 29 );
            LOG.trace( "Language driver code is " + in.readUnsignedByte() );
            in.skipBytes( 2 );
        } else {
            in.seek( 32 );
        }

        LinkedList<Byte> buf = new LinkedList<Byte>();

        LinkedList<PropertyType> types = new LinkedList<PropertyType>();

        int read;
        while ( ( read = in.readUnsignedByte() ) != 13 ) {
            while ( read != 0 && buf.size() < 10 ) {
                buf.add( (byte) read );
                read = in.readUnsignedByte();
            }

            in.skipBytes( 10 - buf.size() );

            byte[] bs = new byte[buf.size()];
            for ( int i = 0; i < bs.length; ++i ) {
                bs[i] = buf.poll();
            }
            String name = getString( bs, encoding );

            char type = (char) in.readUnsignedByte();
            SimplePropertyType pt = null;

            in.skipBytes( 4 );

            int fieldLength = in.readUnsignedByte();
            int fieldPrecision = in.readUnsignedByte();
            LOG.trace( "Field length is " + fieldLength );

            switch ( type ) {
            case 'C':
                if ( fieldPrecision > 0 ) {
                    fieldLength += fieldPrecision << 8;
                    LOG.trace( "Field length is changed to " + fieldLength + " for text field." );
                }
                pt = new SimplePropertyType( new QName( name ), 0, 1, PrimitiveType.STRING, false, null );
                break;
            case 'N':
                pt = new SimplePropertyType( new QName( name ), 0, 1, PrimitiveType.DECIMAL, false, null );
                break;
            case 'L':
                pt = new SimplePropertyType( new QName( name ), 0, 1, PrimitiveType.BOOLEAN, false, null );
                break;
            case 'D':
                pt = new SimplePropertyType( new QName( name ), 0, 1, PrimitiveType.DATE, false, null );
                break;
            case 'F':
                pt = new SimplePropertyType( new QName( name ), 0, 1, PrimitiveType.DECIMAL, false, null );
                break;
            case 'T':
                LOG.warn( "Date/Time fields are not supported. Please send the file to the devs, so they can implement it." );
                break;
            case 'I':
                pt = new SimplePropertyType( new QName( name ), 0, 1, PrimitiveType.INTEGER, false, null );
                break;
            case '@':
                pt = new SimplePropertyType( new QName( name ), 0, 1, PrimitiveType.DATE_TIME, false, null );
                break;
            case 'O':
                LOG.warn( "Double fields are not supported. Please send the file to the devs, so they can implement it." );
                break;
            default:
                LOG.warn( "Exotic field encountered: '" + type
                          + "'. Please send the file to the devs, so they can have a look." );
            }

            LOG.trace( "Found field with name '" + name + "' and type "
                       + ( pt != null ? pt.getPrimitiveType() : " no supported type." ) );

            fields.put( name, new Field( type, pt, fieldLength ) );
            fieldOrder.add( name );
            types.add( pt );

            in.skipBytes( 13 );
            if ( in.readUnsignedByte() == 1 ) {
                LOG.warn( "Index found: index files are not supported by this implementation." );
            }
        }

        types.add( new GeometryPropertyType( new QName( "geometry" ), 0, 1, GEOMETRY, DIM_2_OR_3, false, null, BOTH ) ); // TODO
        // properly
        // determine the
        // dimension from SHP type
        featureType = new GenericFeatureType( ftName, types, false );

    }

    private static String getString( byte[] bs, Charset encoding )
                            throws UnsupportedEncodingException {
        if ( encoding == null ) {
            encoding = guess( bs );
        }
        return new String( bs, encoding );
    }

    /**
     * @param num
     *            zero based
     * @return a map with the property types mapped to their value (which might be null)
     * @throws IOException
     */
    public HashMap<SimplePropertyType, Property<?>> getEntry( int num )
                            throws IOException {
        HashMap<SimplePropertyType, Property<?>> map = new HashMap<SimplePropertyType, Property<?>>();

        long pos = headerLength + num * recordLength;
        if ( pos != in.getFilePointer() ) {
            in.seek( pos );
            pos = headerLength + ( num + 1 ) * recordLength;
        }
        if ( in.readUnsignedByte() == 42 ) {
            LOG.warn( "The record with number " + num + " is marked as deleted." );
        }

        for ( String name : fieldOrder ) {
            Field field = fields.get( name );

            Property<?> property = null;

            byte[] bs = new byte[field.length];
            switch ( field.type ) {
            case 'C': {
                in.readFully( bs );
                property = new GenericProperty<String>( field.propertyType, getString( bs, encoding ).trim() );
                break;
            }
            case 'N':
            case 'F': {
                in.readFully( bs );
                String val = getString( bs, encoding ).trim();
                switch ( field.propertyType.getPrimitiveType() ) {
                case DECIMAL: {
                    property = new GenericProperty<BigDecimal>( field.propertyType, val.isEmpty() ? null
                                                                                                 : new BigDecimal( val ) );
                    break;
                }
                case DOUBLE: {
                    property = new GenericProperty<Double>( field.propertyType, val.isEmpty() ? null : valueOf( val ) );
                    break;
                }
                case INTEGER: {
                    property = new GenericProperty<BigInteger>( field.propertyType, val.isEmpty() ? null
                                                                                                 : new BigInteger( val ) );
                    break;
                }
                }
                break;
            }
            case 'L': {
                char c = (char) in.readUnsignedByte();
                Boolean b = null;
                if ( c == 'Y' || c == 'y' || c == 'T' || c == 't' ) {
                    b = true;
                }
                if ( c == 'N' || c == 'n' || c == 'F' || c == 'f' ) {
                    b = false;
                }
                property = new GenericProperty<Boolean>( field.propertyType, b );
                break;
            }
            case 'D': {
                in.readFully( bs );
                String val = new String( bs, 0, 4 ).trim();
                if ( val.isEmpty() ) {
                    property = new GenericProperty<Calendar>( field.propertyType, null );
                } else {
                    int year = Integer.valueOf( val );
                    int month = Integer.valueOf( new String( bs, 4, 2 ) );
                    int day = Integer.valueOf( new String( bs, 6, 2 ) );
                    Calendar cal = new GregorianCalendar( year, month, day );
                    property = new GenericProperty<Calendar>( field.propertyType, cal );
                }
                break;
            }
            case 'I': {
                int ival = in.readUnsignedByte() + ( in.readUnsignedByte() << 8 ) + ( in.readUnsignedByte() << 16 )
                           + ( in.readUnsignedByte() << 24 );
                property = new GenericProperty<Integer>( field.propertyType, ival );
                break;
            }
            case '@': {
                int days = in.readUnsignedByte() + ( in.readUnsignedByte() << 8 ) + ( in.readUnsignedByte() << 16 )
                           + ( in.readUnsignedByte() << 24 );
                int millis = in.readUnsignedByte() + ( in.readUnsignedByte() << 8 ) + ( in.readUnsignedByte() << 16 )
                             + ( in.readUnsignedByte() << 24 );
                Calendar cal = new GregorianCalendar( -4713, 1, 1 );
                cal.add( DAY_OF_MONTH, days ); // it's lenient by default
                cal.add( MILLISECOND, millis );
                property = new GenericProperty<Calendar>( field.propertyType, cal );
                break;
            }
            case 'T':
            case 'O':
            default:
                LOG.trace( "Skipping unsupported field " + field.propertyType.getName() );
            }

            map.put( field.propertyType, property );
        }

        return map;
    }

    /**
     * Closes the underlying input stream.
     * 
     * @throws IOException
     */
    public void close()
                            throws IOException {
        in.close();
    }

    /**
     * @return the property types of the contained fields
     */
    public LinkedList<PropertyType> getFields() {
        LinkedList<PropertyType> list = new LinkedList<PropertyType>();

        for ( String f : fieldOrder ) {
            list.add( fields.get( f ).propertyType );
        }

        return list;
    }

    class Field {
        char type;

        SimplePropertyType propertyType;

        int length;

        Field( char c, SimplePropertyType pt, int l ) {
            type = c;
            propertyType = pt;
            length = l;
        }
    }

    /**
     * @return the feature type
     */
    public GenericFeatureType getFeatureType() {
        return featureType;
    }

}
