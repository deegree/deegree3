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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.deegree.datatypes.Types;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.io.dbaseapi.DBaseException;
import org.deegree.io.dbaseapi.DBaseFile;
import org.deegree.io.dbaseapi.FieldDescriptor;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.GeometryPropertyType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.spatialschema.Curve;
import org.deegree.model.spatialschema.CurveSegment;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.MultiCurve;
import org.deegree.model.spatialschema.MultiPoint;
import org.deegree.model.spatialschema.MultiSurface;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.Ring;
import org.deegree.model.spatialschema.Surface;

/**
 * <code>ShapeFile</code> encapsulates and provides access to data and properties of a shapefile. Please note that
 * writing will probably fail if the data was read by shapefile.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ShapeFile {

    /**
     * The file type number.
     */
    public static final int FILETYPE = 9994;

    /**
     * The shape file version.
     */
    public static final int VERSION = 1000;

    /**
     * The NULL shape.
     */
    public static final int NULL = 0;

    /**
     * The normal point.
     */
    public static final int POINT = 1;

    /**
     * The normal polyline.
     */
    public static final int POLYLINE = 3;

    /**
     * The normal polygon.
     */
    public static final int POLYGON = 5;

    /**
     * The normal multipoint.
     */
    public static final int MULTIPOINT = 8;

    /**
     * The point with z coordinates.
     */
    public static final int POINTZ = 11;

    /**
     * The polyline with z coordinates.
     */
    public static final int POLYLINEZ = 13;

    /**
     * The polygon with z coordinates.
     */
    public static final int POLYGONZ = 15;

    /**
     * The multipoint with z coordinates.
     */
    public static final int MULTIPOINTZ = 18;

    /**
     * The point with measure.
     */
    public static final int POINTM = 21;

    /**
     * The polyline with measures.
     */
    public static final int POLYLINEM = 23;

    /**
     * The polygon with measures.
     */
    public static final int POLYGONM = 25;

    /**
     * The multipoint with measures.
     */
    public static final int MULTIPOINTM = 28;

    /**
     * The multipatch shape.
     */
    public static final int MULTIPATCH = 31;

    private static final ILogger LOG = LoggerFactory.getLogger( ShapeFile.class );

    private LinkedList<Shape> shapes;

    private ShapeEnvelope envelope;

    private List<FieldDescriptor> descriptors;

    private DBaseFile dbf;

    private String baseName;

    /**
     * @param shapes
     *            the shapes that this shapefile consists of
     * @param envelope
     *            the envelope of all the shapes
     * @param dbf
     *            the associated DBase file
     * @param baseName
     *            the base name
     */
    public ShapeFile( LinkedList<Shape> shapes, ShapeEnvelope envelope, DBaseFile dbf, String baseName ) {
        this.shapes = shapes;
        this.envelope = envelope;
        this.dbf = dbf;
        this.baseName = baseName;
    }

    /**
     * Creates shapefile datastructures from the feature collection.
     *
     * @param fc
     * @param baseName
     *            necessary for DBF creation, base filename without .dbf extension
     * @throws DBaseException
     * @throws GeometryException
     */
    public ShapeFile( FeatureCollection fc, String baseName ) throws DBaseException, GeometryException {
        this.baseName = baseName;
        shapes = new LinkedList<Shape>();

        // get all shapes
        for ( int i = 0; i < fc.size(); ++i ) {
            Feature f = fc.getFeature( i );
            Shape s = extractShape( f );
            shapes.add( s );
            updateEnvelope( s );
        }

        createDBF( fc );
    }

    // this adds the metadata to the dbf
    private void createDBF( FeatureCollection fc )
                            throws DBaseException {
        extractDescriptors( fc );
        dbf = new DBaseFile( baseName, descriptors.toArray( new FieldDescriptor[descriptors.size()] ) );

        for ( int i = 0; i < fc.size(); ++i ) {

            PropertyType[] ftp = fc.getFeature( 0 ).getFeatureType().getProperties();
            ArrayList<Object> list = new ArrayList<Object>( ftp.length );
            for ( int j = 0; j < ftp.length; j++ ) {
                if ( ftp[j].getType() == Types.GEOMETRY ) {
                    continue;
                }
                FeatureProperty fp = fc.getFeature( i ).getDefaultProperty( ftp[j].getName() );
                Object obj = null;
                if ( fp != null ) {
                    obj = fp.getValue();
                }

                if ( obj instanceof Object[] ) {
                    obj = ( (Object[]) obj )[0];
                }

                if ( ( ftp[j].getType() == Types.INTEGER ) || ( ftp[j].getType() == Types.BIGINT )
                     || ( ftp[j].getType() == Types.SMALLINT ) || ( ftp[j].getType() == Types.CHAR )
                     || ( ftp[j].getType() == Types.FLOAT ) || ( ftp[j].getType() == Types.DOUBLE )
                     || ( ftp[j].getType() == Types.NUMERIC ) || ( ftp[j].getType() == Types.VARCHAR )
                     || ( ftp[j].getType() == Types.DATE ) ) {
                    list.add( obj );
                }

            }

            dbf.setRecord( list );
        }

    }

    // updates the envelope upon adding a new shape
    private void updateEnvelope( Shape s ) {
        if ( s.getEnvelope() != null ) {
            if ( envelope == null ) {
                envelope = new ShapeEnvelope( s.getEnvelope() );
            } else {
                envelope.fit( s.getEnvelope() );
            }
        } else {
            if ( s instanceof ShapePoint ) {
                ShapePoint p = (ShapePoint) s;
                // to avoid envelope extension to (0,0,0):
                if ( envelope == null ) {
                    envelope = new ShapeEnvelope( true, false );
                    envelope.xmin = p.x;
                    envelope.ymin = p.y;
                    envelope.zmin = p.z;
                    envelope.xmax = p.x;
                    envelope.ymax = p.y;
                    envelope.zmax = p.z;
                } else {
                    envelope.fit( p.x, p.y, p.z );
                }
            }
        }
    }

    private ArrayList<Curve> getAsCurves( Surface s )
                            throws GeometryException {
        ArrayList<Curve> curves = new ArrayList<Curve>( 10 );

        addAllCurves( s, curves );

        return curves;
    }

    private void addAllCurves( Surface s, List<Curve> curves )
                            throws GeometryException {
        // add exterior ring first
        CurveSegment cs = s.getSurfaceBoundary().getExteriorRing().getAsCurveSegment();
        curves.add( GeometryFactory.createCurve( cs ) );

        // then, add inner rings
        Ring[] innerRings = s.getSurfaceBoundary().getInteriorRings();

        if ( innerRings != null ) {
            for ( Ring r : innerRings ) {
                cs = r.getAsCurveSegment();
                curves.add( GeometryFactory.createCurve( cs ) );
            }
        }
    }

    // currently just the first geometry is extracted, the others are ignored
    private Shape extractShape( Feature f )
                            throws GeometryException {
        Geometry g = f.getDefaultGeometryPropertyValue();

        if ( f.getGeometryPropertyValues().length > 1 ) {
            LOG.logWarning( "Warning, a Feature had more than one Geometries, only the first one is used. Geometry classes:" );
            for ( Geometry g1 : f.getGeometryPropertyValues() ) {
                LOG.logWarning( g1.getClass().getName() );
            }
        }

        if ( g instanceof Point ) {
            return new ShapePoint( (Point) g );
        }

        if ( g instanceof Curve ) {
            return new ShapePolyline( (Curve) g );
        }

        if ( g instanceof Surface ) {
            return new ShapePolygon( getAsCurves( (Surface) g ) );
        }

        if ( g instanceof MultiPoint ) {
            return new ShapeMultiPoint( (MultiPoint) g );
        }

        if ( g instanceof MultiCurve ) {
            List<Curve> cs = Arrays.asList( ( (MultiCurve) g ).getAllCurves() );
            return new ShapePolyline( cs );
        }

        if ( g instanceof MultiSurface ) {
            return new ShapeMultiPatch( (MultiSurface) g );
        }

        return null;
    }

    private void extractDescriptors( FeatureCollection fc )
                            throws DBaseException {
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
            if ( !( obj instanceof ByteArrayInputStream ) && !( obj instanceof Geometry ) ) {
                cnt++;
            }
        }

        // allocate memory for fielddescriptors
        descriptors = new ArrayList<FieldDescriptor>( cnt );

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
                descriptors.add( new FieldDescriptor( s, "N", (byte) 20, (byte) 0 ) );
            } else if ( ftp[i].getType() == Types.BIGINT ) {
                descriptors.add( new FieldDescriptor( s, "N", (byte) 30, (byte) 0 ) );
            } else if ( ftp[i].getType() == Types.SMALLINT ) {
                descriptors.add( new FieldDescriptor( s, "N", (byte) 4, (byte) 0 ) );
            } else if ( ftp[i].getType() == Types.CHAR ) {
                descriptors.add( new FieldDescriptor( s, "C", (byte) 1, (byte) 0 ) );
            } else if ( ftp[i].getType() == Types.FLOAT ) {
                descriptors.add( new FieldDescriptor( s, "N", (byte) 30, (byte) 10 ) );
            } else if ( ftp[i].getType() == Types.DOUBLE || ftp[i].getType() == Types.NUMERIC ) {
                descriptors.add( new FieldDescriptor( s, "N", (byte) 30, (byte) 10 ) );
            } else if ( ftp[i].getType() == Types.VARCHAR ) {
                descriptors.add( new FieldDescriptor( s, "C", (byte) 127, (byte) 0 ) );
            } else if ( ftp[i].getType() == Types.DATE ) {
                descriptors.add( new FieldDescriptor( s, "D", (byte) 12, (byte) 0 ) );
            }
        }

    }

    private FeatureProperty[] getFeatureProperties( FeatureCollection fc, int n ) {
        Feature feature = null;

        feature = fc.getFeature( n );

        PropertyType[] ftp = feature.getFeatureType().getProperties();
        FeatureProperty[] fp = new FeatureProperty[ftp.length];
        FeatureProperty[] fp_ = feature.getProperties();
        for ( int i = 0; i < ftp.length; i++ ) {
            FeatureProperty[] tfp = feature.getProperties( ftp[i].getName() );
            if ( tfp != null && tfp.length > 0 ) {
                fp[i] = FeatureFactory.createFeatureProperty( ftp[i].getName(), fp_[i].getValue() );
            } else {
                fp[i] = FeatureFactory.createFeatureProperty( ftp[i].getName(), "" );
            }
        }

        return fp;
    }

    /**
     * @return the list of shapes contained within this shape file
     */
    public List<Shape> getShapes() {
        return shapes;
    }

    /**
     * @return just the type of the first shape
     */
    public int getShapeType() {
        return shapes.get( 0 ).getType();
    }

    /**
     * @return the sum of all shape sizes plus record header lengths, in bytes
     */
    public int getSize() {
        int len = 0;
        for ( Shape s : shapes ) {
            len += s.getByteLength() + 8;
        }
        return len;
    }

    /**
     * @return the envelope of the shapes.
     */
    public ShapeEnvelope getEnvelope() {
        return envelope;
    }

    /**
     * This writes the DBF file.
     *
     * @throws IOException
     * @throws DBaseException
     */
    public void writeDBF()
                            throws IOException, DBaseException {
        dbf.writeAllToFile();
    }

    /**
     * This method destroys the internal list of shapes and the associated .dbf structure!
     *
     * @return a feature collection with all shapes
     * @throws DBaseException
     */
    public FeatureCollection getFeatureCollection()
                            throws DBaseException {
        FeatureCollection fc = FeatureFactory.createFeatureCollection( baseName, shapes.size() );

        LinkedList<Feature> features = new LinkedList<Feature>();
        for ( int i = 0; i < shapes.size(); ++i ) {
            features.add( dbf.getFRow( i + 1 ) );
        }

        dbf = null;

        int i = 0;
        while ( shapes.size() > 0 ) {
            Shape s = shapes.poll();
            Feature feature = features.poll();
            if ( i % 10000 == 0 ) {
                System.out.print( i + " shapes processed.\r" );
            }

            Geometry geo = s.getGeometry();

            GeometryPropertyType[] geoPTs = feature.getFeatureType().getGeometryProperties();
            for ( GeometryPropertyType pt : geoPTs ) {
                FeatureProperty[] geoProp = feature.getProperties( pt.getName() );
                for ( int j = 0; j < geoProp.length; j++ ) {
                    geoProp[j].setValue( geo );
                }
            }

            fc.add( feature );
            ++i;
        }

        LOG.logInfo( i + " shapes processed in total." );

        return fc;
    }

    /**
     * @return the base name of this shape file
     */
    public String getBaseName() {
        return baseName;
    }

}
