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
package org.deegree.io.shpapi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.io.dbaseapi.DBaseException;
import org.deegree.io.dbaseapi.DBaseFile;
import org.deegree.io.dbaseapi.DBaseIndex;
import org.deegree.io.dbaseapi.DBaseIndexException;
import org.deegree.io.dbaseapi.FieldDescriptor;
import org.deegree.io.rtree.HyperBoundingBox;
import org.deegree.io.rtree.HyperPoint;
import org.deegree.io.rtree.RTree;
import org.deegree.io.rtree.RTreeException;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.GeometryPropertyType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.spatialschema.ByteUtils;
import org.deegree.model.spatialschema.Curve;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.JTSAdapter;
import org.deegree.model.spatialschema.MultiCurve;
import org.deegree.model.spatialschema.MultiPoint;
import org.deegree.model.spatialschema.MultiSurface;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.Ring;
import org.deegree.model.spatialschema.Surface;
import org.deegree.model.spatialschema.SurfaceInterpolationImpl;

import com.vividsolutions.jts.algorithm.CGAlgorithms;

/**
 * Class representing an ESRI Shape File.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ShapeFile {

    private DBaseFile dbf = null;

    private SHP2WKS shpwks = new SHP2WKS();

    /*
     * contains the dBase indexes
     */
    private Hashtable<String, DBaseIndex> dBaseIndexes = new Hashtable<String, DBaseIndex>( 50 );

    /*
     * aggregated Instance-variables
     */
    private MainFile shp = null;

    private RTree rti = null;

    private String fileName = null;

    /*
     * indicates if a dBase-file is associated to the shape-file
     */
    private boolean hasDBaseFile = true;

    /*
     * indicates if an R-tree index is associated to the shape-file
     */
    private boolean hasRTreeIndex = true;

    /**
     * constructor: <BR>
     * Construct a ShapeFile from a file name.<BR>
     * 
     * @param fileName
     * @throws IOException
     */
    public ShapeFile( String fileName ) throws IOException {
        this.fileName = fileName;
        /*
         * initialize the MainFile
         */
        shp = new MainFile( fileName );

        /*
         * initialize the DBaseFile
         */
        try {
            dbf = new DBaseFile( fileName );
        } catch ( IOException e ) {
            hasDBaseFile = false;
        }

        /*
         * initialize the RTreeIndex
         */
        try {
            rti = new RTree( fileName + ".rti" );
        } catch ( RTreeException e ) {
            hasRTreeIndex = false;
        }

        if ( hasDBaseFile ) {
            String[] s = null;
            try {
                s = getProperties();
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            for ( int i = 0; i < s.length; i++ ) {
                try {
                    dBaseIndexes.put( s[i], new DBaseIndex( fileName + "$" + s[i] ) );
                } catch ( IOException e ) {
                    // uh, oh
                }
            }
        }
    }

    /**
     * constructor: <BR>
     * Construct a ShapeFile from a file name.<BR>
     * 
     * @param url
     * @param rwflag
     * @throws IOException
     */
    public ShapeFile( String url, String rwflag ) throws IOException {
        this.fileName = url;
        shp = new MainFile( url, rwflag );
        // TODO: initialize dbf, rti
        hasDBaseFile = false;
        hasRTreeIndex = false;
    }

    /**
     *
     */
    public void close() {

        shp.close();
        dbf.close();

        if ( rti != null ) {
            try {
                rti.close();
            } catch ( Exception e ) {
                // should never happen
                e.printStackTrace();
            }
        }

        for ( Enumeration<DBaseIndex> e = dBaseIndexes.elements(); e.hasMoreElements(); ) {
            DBaseIndex index = e.nextElement();

            try {
                index.close();
            } catch ( Exception ex ) {
                // uh, oh
            }
        }
    }

    /**
     * Overrides the default feature type (which is generated from all columns in the dbase file) to allow customized
     * naming and ordering of properties.
     * 
     * @param ft
     * @param ftMapping
     */
    public void setFeatureType( FeatureType ft, Map<PropertyType, String> ftMapping ) {
        dbf.setFeatureType( ft, ftMapping );
    }

    /**
     * returns true if a column is indexed
     * 
     * @param column
     * @return true if a column is indexed
     */
    public boolean hasDBaseIndex( String column ) {
        DBaseIndex index = dBaseIndexes.get( column );
        return index != null;
    }

    /**
     * returns true if a dBase-file is associated to the shape-file<BR>
     * 
     * @return true if a dBase-file is associated to the shape-file<BR>
     */
    public boolean hasDBaseFile() {
        return this.hasDBaseFile;
    }

    /**
     * returns true if an R-tree index is associated to the shape-file<BR>
     * 
     * @return true if an R-tree index is associated to the shape-file<BR>
     */
    public boolean hasRTreeIndex() {
        return this.hasRTreeIndex;
    }

    /**
     * returns the number of records within a shape-file<BR>
     * 
     * @return the number of records within a shape-file<BR>
     */
    public int getRecordNum() {
        return shp.getRecordNum();
    }

    /**
     * returns the minimum bounding rectangle of all geometries<BR>
     * within the shape-file
     * 
     * @return the minimum bounding rectangle of all geometries<BR>
     *         within the shape-file
     */
    public Envelope getFileMBR() {
        double xmin = shp.getFileMBR().west;
        double xmax = shp.getFileMBR().east;
        double ymin = shp.getFileMBR().south;
        double ymax = shp.getFileMBR().north;

        return GeometryFactory.createEnvelope( xmin, ymin, xmax, ymax, null );
    }

    /**
     * returns the minimum bound rectangle of RecNo'th Geometrie<BR>
     * 
     * @param recNo
     * @return the minimum bound rectangle of RecNo'th Geometrie<BR>
     * @throws IOException
     */
    public Envelope getMBRByRecNo( int recNo )
                            throws IOException {
        SHPEnvelope shpenv = shp.getRecordMBR( recNo );
        if ( shpenv != null ) {
            double xmin = shpenv.west;
            double xmax = shpenv.east;
            double ymin = shpenv.south;
            double ymax = shpenv.north;

            return GeometryFactory.createEnvelope( xmin, ymin, xmax, ymax, null );
        } else {
            return null;
        }
    }

    /**
     * Returns the given entry of the shape file as a {@link Feature} instance.
     * <p>
     * This contains the geometry as well as the attributes stored into the dbase file. The geometry property will use a
     * default name (app:GEOM).
     * 
     * @param RecNo
     * @return the given entry of the shape file as a Feature instance
     * @throws IOException
     * @throws HasNoDBaseFileException
     * @throws DBaseException
     */
    public Feature getFeatureByRecNo( int RecNo )
                            throws IOException, HasNoDBaseFileException, DBaseException {

        if ( !hasDBaseFile ) {
            throw new HasNoDBaseFileException( "Exception: there is no dBase-file " + "associated to this shape-file" );
        }

        // get feature (without geometry property) from DBaseFile
        Feature feature = dbf.getFRow( RecNo );

        // exchange null geometries with real geometry
        Geometry geo = getGeometryByRecNo( RecNo );
        GeometryPropertyType[] geoPTs = feature.getFeatureType().getGeometryProperties();
        for ( int i = 0; i < geoPTs.length; i++ ) {
            FeatureProperty[] geoProp = feature.getProperties( geoPTs[i].getName() );
            for ( int j = 0; j < geoProp.length; j++ ) {
                geoProp[j].setValue( geo );
            }
        }

        return feature;
    }

    /**
     * returns RecNo'th Geometrie<BR>
     * 
     * @param recNo
     * @return RecNo'th Geometrie<BR>
     * @throws IOException
     */
    public Geometry getGeometryByRecNo( int recNo )
                            throws IOException {
        Geometry geom = null;

        int shpType = getShapeTypeByRecNo( recNo );

        if ( shpType == ShapeConst.SHAPE_TYPE_POINT ) {
            SHPPoint shppoint = (SHPPoint) shp.getByRecNo( recNo );
            geom = shpwks.transformPoint( null, shppoint );
        } else if ( shpType == ShapeConst.SHAPE_TYPE_MULTIPOINT ) {
            SHPMultiPoint shpmultipoint = (SHPMultiPoint) shp.getByRecNo( recNo );
            Point[] points = shpwks.transformMultiPoint( null, shpmultipoint );
            if ( points != null ) {
                MultiPoint mp = GeometryFactory.createMultiPoint( points );
                geom = mp;
            } else {
                geom = null;
            }
        } else if ( shpType == ShapeConst.SHAPE_TYPE_POLYLINE ) {
            SHPPolyLine shppolyline = (SHPPolyLine) shp.getByRecNo( recNo );
            Curve[] curves = shpwks.transformPolyLine( null, shppolyline );
            if ( ( curves != null ) && ( curves.length > 1 ) ) {
                // create multi curve
                MultiCurve mc = GeometryFactory.createMultiCurve( curves );
                geom = mc;
            } else if ( ( curves != null ) && ( curves.length == 1 ) ) {
                // single curve
                geom = curves[0];
            } else {
                geom = null;
            }
        } else if ( shpType == ShapeConst.SHAPE_TYPE_POLYGON ) {
            SHPPolygon shppoly = (SHPPolygon) shp.getByRecNo( recNo );
            Surface[] polygons = shpwks.transformPolygon( null, shppoly );
            if ( ( polygons != null ) && ( polygons.length > 1 ) ) {
                // create multi surface
                MultiSurface ms = GeometryFactory.createMultiSurface( polygons );
                geom = ms;
            } else if ( ( polygons != null ) && ( polygons.length == 1 ) ) {
                geom = polygons[0];
            } else {
                geom = null;
            }
        } else if ( shpType == ShapeConst.SHAPE_TYPE_POLYGONZ ) {

            SHPPolygon3D shppoly = (SHPPolygon3D) shp.getByRecNo( recNo );

            Surface[] polygons = shpwks.transformPolygon( null, shppoly );

            if ( ( polygons != null ) && ( polygons.length > 1 ) ) {
                // create multi surface
                MultiSurface ms = GeometryFactory.createMultiSurface( polygons );
                geom = ms;
            } else if ( ( polygons != null ) && ( polygons.length == 1 ) ) {
                geom = polygons[0];
            } else {
                geom = null;
            }
        }

        return geom;
    }

    /**
     * returns the type of the RecNo'th Geometrie<BR>
     * per definition a shape file contains onlay one shape type<BR>
     * but null shapes are possible too!<BR>
     * 
     * @param RecNo
     * @return the type of the RecNo'th Geometrie<BR>
     *         per definition a shape file contains onlay one shape type<BR>
     *         but null shapes are possible too!<BR>
     * @throws IOException
     */
    public int getShapeTypeByRecNo( int RecNo )
                            throws IOException {
        return shp.getShapeTypeByRecNo( RecNo );
    }

    /**
     * returns a int array that containts all the record numbers that matches the search operation
     * 
     * @param column
     * @param value
     * @return a int array that containts all the record numbers that matches the search operation
     * @throws IOException
     * @throws DBaseIndexException
     */
    public int[] getGeoNumbersByAttribute( String column, Comparable<?> value )
                            throws IOException, DBaseIndexException {
        DBaseIndex index = dBaseIndexes.get( column );

        if ( index == null ) {
            return null;
        }

        return index.search( value );
    }

    /**
     * returns a ArrayList that contains all geomeries of the shape file<BR>
     * which mbr's are completly or partly within the rectangle r<BR>
     * only Points, MultiPoints, PolyLines and Polygons are handled<BR>
     * 
     * @param r
     * @return a ArrayList that contains all geomeries of the shape file<BR>
     *         which mbr's are completly or partly within the rectangle r<BR>
     *         only Points, MultiPoints, PolyLines and Polygons are handled<BR>
     * @throws IOException
     */
    public int[] getGeoNumbersByRect( Envelope r )
                            throws IOException {
        SHPPoint geom = null;
        int[] num = null;
        int numRecs = getRecordNum();

        Envelope mbr = getFileMBR();

        if ( !mbr.intersects( r ) ) {
            return null;
        }

        if ( hasRTreeIndex ) {
            try {
                // translate envelope (deegree) to bounding box (rtree)
                HyperBoundingBox box = new HyperBoundingBox( new HyperPoint( r.getMin().getAsArray() ),
                                                             new HyperPoint( r.getMax().getAsArray() ) );
                Object[] iNumbers = rti.intersects( box );
                num = new int[iNumbers.length];

                for ( int i = 0; i < iNumbers.length; i++ )
                    num[i] = ( (Integer) iNumbers[i] ).intValue();

                return num;
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }

        // for every geometry (record) within the shape file
        // check if it's inside the search-rectangle r
        List<Integer> numbers = new ArrayList<Integer>( 500 );
        for ( int i = 0; i < numRecs; i++ ) {
            if ( getShapeTypeByRecNo( i + 1 ) == ShapeConst.SHAPE_TYPE_NULL ) {
                // why is nothing done here?
            } else if ( getShapeTypeByRecNo( i + 1 ) == ShapeConst.SHAPE_TYPE_POINT ) {
                geom = (SHPPoint) shp.getByRecNo( i + 1 );

                // is the Point within the seach rectangle?
                Position pos = GeometryFactory.createPosition( geom.x, geom.y );

                if ( r.contains( pos ) == true ) {
                    numbers.add( new Integer( i + 1 ) );
                }
            } else {
                // get minimum bounding rectangle of the i'th record
                mbr = getMBRByRecNo( i + 1 );

                // is the i'th record a geometrie having a mbr
                // (only for PolyLines, Polygons and MultiPoints mbrs are defined)
                if ( mbr != null ) {
                    // if the tested rectangles are not disjunct the number of the
                    // actual record is added to the ArrayList
                    if ( mbr.intersects( r ) ) {
                        numbers.add( new Integer( i + 1 ) );
                    }
                }
            }
        }

        if ( numbers.size() > 0 ) {
            num = new int[numbers.size()];

            // put all numbers within numbers to an array
            for ( int i = 0; i < numbers.size(); i++ ) {
                num[i] = numbers.get( i );
            }
        }

        return num;
    } // end of getGeoNumbersByRect

    /**
     * is a property unique?
     * 
     * @param property
     * @return if it is unique
     */
    public boolean isUnique( String property ) {
        DBaseIndex index = dBaseIndexes.get( property );

        if ( index == null ) {
            return false;
        }

        return index.isUnique();
    }

    /**
     * returns the properties (column headers) of the dBase-file<BR>
     * associated to the shape-file<BR>
     * 
     * @return the properties (column headers) of the dBase-file<BR>
     *         associated to the shape-file<BR>
     * @throws HasNoDBaseFileException
     * @throws DBaseException
     */
    public String[] getProperties()
                            throws HasNoDBaseFileException, DBaseException {
        if ( !hasDBaseFile ) {
            throw new HasNoDBaseFileException( "Exception: there is no dBase-file " + "associated to this shape-file" );
        }

        return dbf.getProperties();
    }

    /**
     * returns the datatype of each column of the database file<BR>
     * associated to the shape-file<BR>
     * 
     * @return the datatype of each column of the database file<BR>
     *         associated to the shape-file<BR>
     * @throws HasNoDBaseFileException
     * @throws DBaseException
     */
    public String[] getDataTypes()
                            throws HasNoDBaseFileException, DBaseException {
        if ( !hasDBaseFile ) {
            throw new HasNoDBaseFileException( "Exception: there is no dBase-file " + "associated to this shape-file" );
        }

        return dbf.getDataTypes();
    }

    /**
     * 
     * 
     * @return the lengths
     * 
     * @throws HasNoDBaseFileException
     * @throws DBaseException
     */
    public int[] getDataLengths()
                            throws HasNoDBaseFileException, DBaseException {
        String[] properties = getProperties();
        int[] retval = new int[properties.length];

        for ( int i = 0; i < properties.length; i++ ) {
            retval[i] = dbf.getDataLength( properties[i] );
        }

        return retval;
    }

    /**
     * returns the datatype of each column of the dBase associated<BR>
     * to the shape-file specified by fields<BR>
     * 
     * @param fields
     * @return the datatype of each column of the dBase associated<BR>
     *         to the shape-file specified by fields<BR>
     * @throws HasNoDBaseFileException
     * @throws DBaseException
     */
    public String[] getDataTypes( String[] fields )
                            throws HasNoDBaseFileException, DBaseException {
        if ( !hasDBaseFile ) {
            throw new HasNoDBaseFileException( "Exception: there is no dBase-file " + "associated to this shape-file" );
        }

        return dbf.getDataTypes( fields );
    }

    /**
     * returns the number of geometries within a feature collection<BR>
     * 
     * @param fc
     *            : featurecollection which is checked for the number geomtries<BR>
     */
    private int getGeometryCount( FeatureCollection fc ) {
        return fc.size();
    }

    /**
     * returns the type of the n'th feature in a featurecollection
     * 
     * @param fc
     *            FeatureCollection. must not be empty.
     * @param n
     *            number of the feature which should be examined starts with 0
     */
    private int getGeometryType( FeatureCollection fc, int n ) {
        Feature feature = null;

        feature = fc.getFeature( n );

        Geometry[] g = feature.getGeometryPropertyValues();

        if ( ( g == null ) || ( g.length == 0 ) ) {
            return -1;
        }

        if ( g[0] instanceof Point ) {
            return 0;
        }

        if ( g[0] instanceof Curve ) {
            return 1;
        }

        if ( g[0] instanceof Surface ) {
            return 2;
        }

        if ( g[0] instanceof MultiPoint ) {
            return 3;
        }

        if ( g[0] instanceof MultiCurve ) {
            return 4;
        }

        if ( g[0] instanceof MultiSurface ) {
            return 5;
        }

        return -1;
    }

    /**
     * returns the n'th feature of a featurecollection as a Geometry<BR>
     * 
     * @param fc
     *            : FeatureCollection<BR>
     * @param n
     *            : number of the feature which should be returned<BR>
     */
    private Geometry getFeatureAsGeometry( FeatureCollection fc, int n ) {
        Feature feature = null;

        feature = fc.getFeature( n );

        return feature.getGeometryPropertyValues()[0];
    }

    /**
     * Copies the feature properties of a feature.
     * 
     * @param fc
     * @param n
     *            the number of the feature to get the properties for
     * @return the properties
     */
    public FeatureProperty[] getFeatureProperties( FeatureCollection fc, int n ) {
        Feature feature = fc.getFeature( n );

        PropertyType[] ftp = feature.getFeatureType().getProperties();
        FeatureProperty[] fp = new FeatureProperty[ftp.length];
        
        for ( int i = 0; i < ftp.length; i++ ) {
            FeatureProperty fp_ = feature.getDefaultProperty( ftp[i].getName() );
            Object value = null;
            if ( fp_ != null ) {
                value = fp_.getValue();
            }
            fp[i] = FeatureFactory.createFeatureProperty( ftp[i].getName(), value );
        }

        return fp;
    }

    /**
     */
    private void initDBaseFile( FeatureCollection fc )
                            throws DBaseException {
        FieldDescriptor[] fieldDesc = null;

        // get feature properties
        FeatureProperty[] pairs = getFeatureProperties( fc, 0 );

        // count regular fields
        int cnt = 0;
        FeatureType featT = fc.getFeature( 0 ).getFeatureType();
        PropertyType[] ftp = featT.getProperties();
              
        for ( int i = 0; i < pairs.length; i++ ) {
            Object obj = pairs[i].getValue();

            if ( obj instanceof Object[] ) {
                obj = ( (Object[]) obj )[0];
            }
            PropertyType pt = featT.getProperty( pairs[i].getName() );
            if ( !( obj instanceof ByteArrayInputStream ) && !( obj instanceof Geometry ) && pt.getType() != Types.GEOMETRY ) {
                cnt++;
            }
        }

        // allocate memory for field descriptors
        fieldDesc = new FieldDescriptor[cnt];

        // determine maximum field length for string columns
        Iterator<Feature> iter = fc.iterator();
        Map<QualifiedName, Integer> fieldLength = new HashMap<QualifiedName, Integer>();
        while ( iter.hasNext() ) {
            Feature feature = (Feature) iter.next();
            FeatureProperty[] fp = feature.getProperties();
            for ( FeatureProperty featureProperty : fp ) {
                Object o = null;
                if ( featureProperty != null && ( o = featureProperty.getValue() ) instanceof String ) {
                    int k = ( (String) o ).length();
                    if ( fieldLength.get( featureProperty.getName() ) == null || k > fieldLength.get( featureProperty.getName() ) ) {
                        fieldLength.put( featureProperty.getName(), new Integer( k + 5) );
                    }
                }
            }

        }
        // get properties names and types and create a FieldDescriptor
        // for each properties except the geometry-property
        cnt = 0;
        for ( int i = 0; i < ftp.length; i++ ) {
            int pos = ftp[i].getName().getLocalName().lastIndexOf( '.' );
            if ( pos < 0 ) {
                pos = -1;
            }
            String s = ftp[i].getName().getLocalName().substring( pos + 1 );
            if ( ftp[i].getType() == Types.INTEGER ) {
                fieldDesc[cnt++] = new FieldDescriptor( s, "N", (byte) 20, (byte) 0 );
            } else if ( ftp[i].getType() == Types.BIGINT ) {
                fieldDesc[cnt++] = new FieldDescriptor( s, "N", (byte) 30, (byte) 0 );
            } else if ( ftp[i].getType() == Types.SMALLINT ) {
                fieldDesc[cnt++] = new FieldDescriptor( s, "N", (byte) 4, (byte) 0 );
            } else if ( ftp[i].getType() == Types.CHAR ) {
                fieldDesc[cnt++] = new FieldDescriptor( s, "C", (byte) 1, (byte) 0 );
            } else if ( ftp[i].getType() == Types.FLOAT ) {
                fieldDesc[cnt++] = new FieldDescriptor( s, "N", (byte) 30, (byte) 10 );
            } else if ( ftp[i].getType() == Types.DOUBLE || ftp[i].getType() == Types.NUMERIC ) {
                fieldDesc[cnt++] = new FieldDescriptor( s, "N", (byte) 30, (byte) 10 );
            } else if ( ftp[i].getType() == Types.DECIMAL ) {
                fieldDesc[cnt++] = new FieldDescriptor( s, "N", (byte) 30, (byte) 10 );
            } else if ( ftp[i].getType() == Types.VARCHAR ) {
                int len = 10;
                if ( fieldLength.get( ftp[i].getName() ) != null ) {
                    len = fieldLength.get( ftp[i].getName() ).intValue()+2;    
                }                
                if ( len > 127 ) {
                    len = 127;
                }
                fieldDesc[cnt++] = new FieldDescriptor( s, "C", (byte)len, (byte) 0 );
            } else if ( ftp[i].getType() == Types.DATE ) {
                fieldDesc[cnt++] = new FieldDescriptor( s, "D", (byte) 12, (byte) 0 );
            } else {
            }
        }
        // initialize/create DBaseFile
        try {
            dbf = new DBaseFile( fileName, fieldDesc );
        } catch ( DBaseException e ) {
            hasDBaseFile = false;
        }
    }

    /**
     * writes a OGC FeatureCollection to a ESRI shape file.<BR>
     * all features in the collection must have the same properties.<BR>
     * 
     * @param fc
     *            FeatureCollection. must not be null or empty.
     * @throws Exception
     */
    public void writeShape( FeatureCollection fc )
                            throws Exception {

        int nbyte = 0;
        int geotype = -1;
        byte shptype = -1;
        int typ_ = getGeometryType( fc, 0 );
        byte[] bytearray = null;
        IndexRecord record = null;
        SHPEnvelope mbr = null;
        // mbr of the whole shape file
        SHPEnvelope shpmbr = new SHPEnvelope();

        // Set the Offset to the end of the fileHeader
        int offset = ShapeConst.SHAPE_FILE_HEADER_LENGTH;

        // initialize the dbasefile associated with the shapefile
        initDBaseFile( fc );

        // loop throug the Geometries of the feature collection anf write them
        // to a bytearray
        for ( int i = 0; i < getGeometryCount( fc ); i++ ) {
            if ( i % 1000 == 0 ) {
                System.gc();
            }
            // write i'th features properties to a ArrayList
            PropertyType[] ftp = fc.getFeature( 0 ).getFeatureType().getProperties();
            ArrayList<Object> vec = new ArrayList<Object>( ftp.length );
            for ( int j = 0; j < ftp.length; j++ ) {
                if ( ftp[j].getType() == Types.GEOMETRY )
                    continue;
                FeatureProperty fp = fc.getFeature( i ).getDefaultProperty( ftp[j].getName() );
                Object obj = null;
                if ( fp != null ) {
                    obj = fp.getValue();
                }

                if ( obj instanceof Object[] ) {
                    obj = ( (Object[]) obj )[0];
                }

                if ( ( ftp[j].getType() == Types.INTEGER ) || ( ftp[j].getType() == Types.BIGINT )
                     || ( ftp[j].getType() == Types.DECIMAL )
                     || ( ftp[j].getType() == Types.SMALLINT ) || ( ftp[j].getType() == Types.CHAR )
                     || ( ftp[j].getType() == Types.FLOAT ) || ( ftp[j].getType() == Types.DOUBLE )
                     || ( ftp[j].getType() == Types.NUMERIC ) || ( ftp[j].getType() == Types.VARCHAR )
                     || ( ftp[j].getType() == Types.DATE ) ) {
                    vec.add( obj );
                }

            }

            // write the ArrayList (properties) to the dbase file
            try {
                dbf.setRecord( vec );
            } catch ( DBaseException db ) {
                db.printStackTrace();
                throw new Exception( db.toString() );
            }

            // Get Geometry Type of i'th feature
            geotype = getGeometryType( fc, i );

            if ( geotype < 0 ) {
                continue;
            }

            if ( ( typ_ == 0 ) || ( typ_ == 3 ) ) {
                if ( ( geotype != 0 ) && ( geotype != 3 ) ) {
                    throw new Exception( "Not a homogen FeatureCollection" );
                }
            }

            if ( ( typ_ == 1 ) || ( typ_ == 4 ) ) {
                if ( ( geotype != 1 ) && ( geotype != 4 ) ) {
                    throw new Exception( "Not a homogen FeatureCollection" );
                }
            }

            if ( ( typ_ == 2 ) || ( typ_ == 5 ) ) {
                if ( ( geotype != 2 ) && ( geotype != 5 ) ) {
                    throw new Exception( "Not a homogen FeatureCollection" );
                }
            }

            // get wks geometrie for feature (i) and write it to a file
            if ( geotype == 0 ) {
                // Geometrie Type = Point
                Point wks = (Point) getFeatureAsGeometry( fc, i );
                SHPPoint shppoint = new SHPPoint( wks.getPosition() );
                nbyte = shppoint.size();
                bytearray = new byte[nbyte + ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH];
                shppoint.writeSHPPoint( bytearray, ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH );
                mbr = new SHPEnvelope( shppoint, shppoint );

                if ( i == 0 ) {
                    shpmbr = mbr;
                }

                shptype = 1;
            } else if ( geotype == 1 ) {
                // Geometrie Type = LineString
                Curve[] wks = new Curve[1];
                wks[0] = (Curve) getFeatureAsGeometry( fc, i );

                SHPPolyLine shppolyline = new SHPPolyLine( wks );
                nbyte = shppolyline.size();
                bytearray = new byte[nbyte + ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH];
                shppolyline.writeSHPPolyLine( bytearray, ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH );
                mbr = shppolyline.getEnvelope();

                if ( i == 0 ) {
                    shpmbr = mbr;
                }

                shptype = 3;
            } else if ( geotype == 2 ) {
                // Geometrie Type = Polygon
                Surface[] wks = new Surface[1];
                wks[0] = (Surface) getFeatureAsGeometry( fc, i );
                validateOrientation( wks );

                SHPPolygon shppolygon = new SHPPolygon( wks );
                nbyte = shppolygon.size();
                bytearray = new byte[nbyte + ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH];
                shppolygon.writeSHPPolygon( bytearray, ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH );
                mbr = shppolygon.getEnvelope();

                if ( i == 0 ) {
                    shpmbr = mbr;
                }

                shptype = 5;
            } else if ( geotype == 3 ) {
                // Geometrie Type = MultiPoint
                MultiPoint wks = (MultiPoint) getFeatureAsGeometry( fc, i );
                SHPMultiPoint shpmultipoint = new SHPMultiPoint( wks );
                nbyte = shpmultipoint.size();
                bytearray = new byte[nbyte + ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH];
                shpmultipoint.writeSHPMultiPoint( bytearray, ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH );
                mbr = shpmultipoint.getEnvelope();
                shptype = 8;
            } else if ( geotype == 4 ) {
                // Geometrie Type = MultiLineString
                MultiCurve wks = (MultiCurve) getFeatureAsGeometry( fc, i );
                SHPPolyLine shppolyline = new SHPPolyLine( wks.getAllCurves() );
                nbyte = shppolyline.size();
                bytearray = new byte[nbyte + ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH];
                shppolyline.writeSHPPolyLine( bytearray, ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH );
                mbr = shppolyline.getEnvelope();

                if ( i == 0 ) {
                    shpmbr = mbr;
                }

                shptype = 3;
            } else if ( geotype == 5 ) {
                // Geometrie Type = MultiPolygon
                MultiSurface wks = (MultiSurface) getFeatureAsGeometry( fc, i );
                SHPPolygon shppolygon = new SHPPolygon( wks.getAllSurfaces() );
                nbyte = shppolygon.size();
                bytearray = new byte[nbyte + ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH];
                shppolygon.writeSHPPolygon( bytearray, ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH );
                mbr = shppolygon.getEnvelope();

                if ( i == 0 ) {
                    shpmbr = mbr;
                }

                shptype = 5;
            }

            // write bytearray to the shape file
            record = new IndexRecord( offset / 2, nbyte / 2 );

            // write recordheader to the bytearray
            ByteUtils.writeBEInt( bytearray, 0, i );
            ByteUtils.writeBEInt( bytearray, 4, nbyte / 2 );

            // write record (bytearray) including recordheader to the shape file
            shp.write( bytearray, record, mbr );

            // actualise shape file minimum boundary rectangle
            if ( mbr.west < shpmbr.west ) {
                shpmbr.west = mbr.west;
            }

            if ( mbr.east > shpmbr.east ) {
                shpmbr.east = mbr.east;
            }

            if ( mbr.south < shpmbr.south ) {
                shpmbr.south = mbr.south;
            }

            if ( mbr.north > shpmbr.north ) {
                shpmbr.north = mbr.north;
            }

            // icrement offset for pointing at the end of the file
            offset += ( nbyte + ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH );

            bytearray = null;
        }

        dbf.writeAllToFile();

        // write shape header
        shp.writeHeader( offset, shptype, shpmbr );

    }

    private void validateOrientation( Surface[] wks )
                            throws GeometryException {
        com.vividsolutions.jts.geom.Geometry jts = JTSAdapter.export( wks[0] );
        CGAlgorithms.isCCW( jts.getCoordinates() );
        if ( CGAlgorithms.isCCW( jts.getCoordinates() ) ) {
            Position[] pos = wks[0].getSurfaceBoundary().getExteriorRing().getPositions();
            Position[] pos2 = new Position[pos.length];
            for ( int j = 0; j < pos2.length; j++ ) {
                pos2[j] = pos[pos.length - j - 1];
            }
            Position[][] iPos = null;
            if ( wks[0].getSurfaceBoundary().getInteriorRings() != null ) {
                Ring[] rings = wks[0].getSurfaceBoundary().getInteriorRings();
                iPos = new Position[rings.length][];
                for ( int j = 0; j < rings.length; j++ ) {
                    pos = rings[j].getPositions();
                    iPos[j] = new Position[pos.length];
                    for ( int k = 0; k < pos.length; k++ ) {
                        iPos[j][k] = pos[pos.length - k - 1];
                    }
                }
            }
            wks[0] = GeometryFactory.createSurface( pos2, iPos, new SurfaceInterpolationImpl(),
                                                    wks[0].getCoordinateSystem() );
        }
    }
}
