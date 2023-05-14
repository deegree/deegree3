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
package org.deegree.services.wps.provider.sextante;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.dataObjects.AbstractVectorLayer;
import es.unex.sextante.dataObjects.FeatureImpl;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;

/**
 * Manages features to execute a SEXTANTE {@link GeoAlgorithm}.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * 
 */

public class VectorLayerImpl extends AbstractVectorLayer {

    private static final Logger LOG = LoggerFactory.getLogger( VectorLayerImpl.class );

    private String m_Name; // layer name

    private String m_CRS; // coordinate reference System

    private Field[] m_Fields; // fields with name and data type

    private LinkedList<IFeature> m_Features = new LinkedList<IFeature>(); // features

    private int m_ShapeType = -2; // ShapeType of IVectorLayer

    /**
     * Creates a VectorLayer with the coordinate reference System <br>
     * "EPSG:4326" and the name "VectorLayer". <br>
     * No attributes are provided for the geometries.
     */
    public VectorLayerImpl() {
        this( null, null );
    }

    /**
     * Creates a VectorLayer. <br>
     * No attributes are provided for the geometries.
     * 
     * @param name
     *            Layer name.
     * @param crs
     *            Coordinate reference system.
     */
    public VectorLayerImpl( String name, String crs ) {
        this( name, crs, null );
    }

    /**
     * Creates a VectorLayer.
     * 
     * @param name
     *            Layer name.
     * @param crs
     *            Coordinate reference system.
     * @param fields
     *            Name and data type of geometry attributes.
     */
    public VectorLayerImpl( String name, String crs, Field[] fields ) {
        m_Name = name;
        m_CRS = crs;
        m_Fields = fields;

        if ( name == null ) {
            m_Name = "VectorLayer";
        }

        if ( crs == null ) {
            m_CRS = "EPSG:4326";
        }

        if ( fields == null ) {
            m_Fields = new Field[] {};
        }

    }

    @Override
    public void addFeature( Geometry geometry, Object[] attributes ) {

        if ( attributes == null ) {
            attributes = new Object[] {};
        }

        // attribute count correct
        if ( attributes.length == m_Fields.length ) {
            setShapeType( geometry );
            m_Features.add( new FeatureImpl( geometry, attributes ) );

        } else { // attribute count not correct
            LOG.error( "addFeature(): Values are not correct." );
            throw new ArrayIndexOutOfBoundsException();
        }

    }

    @Override
    public int getFieldCount() {
        return m_Fields.length;
    }

    @Override
    public String getFieldName( int index ) {
        return m_Fields[index].getNameWithNamespaceAndPrefix();
    }

    @Override
    public Class<?> getFieldType( int index ) {
        return m_Fields[index].getType();
    }

    /**
     * Returns the {@link Field}.
     * 
     * @param index
     *            Index of the {@link Field}.
     * 
     * @return {@link Field}.
     */
    public Field getField( int index ) {
        return m_Fields[index];
    }

    /**
     * Returns all {@link Field}s.
     * 
     * @return All {@link Field}s.
     */
    public Field[] getFields() {
        return m_Fields;
    }

    @Override
    public int getShapeType() {
        return m_ShapeType;
    }

    @Override
    public int getShapesCount() {
        return m_Features.size();
    }

    @Override
    public IFeatureIterator iterator() {
        return new FeatureIteratorImpl( m_Features.iterator() );
    }

    @Override
    public Object getCRS() {
        return m_CRS;
    }

    @Override
    public Rectangle2D getFullExtent() {

        // Create a geometry array
        Geometry[] geoms = new Geometry[m_Features.size()];
        Iterator<IFeature> it = m_Features.iterator();
        for ( int i = 0; i < geoms.length; i++ ) {
            geoms[i] = it.next().getGeometry();
        }

        // Create a GeometryCollection
        GeometryCollection coll = new GeometryCollection( geoms, new GeometryFactory() );

        // Create envelope of all features
        Envelope env = coll.getEnvelopeInternal();

        // Return Rectangle
        if ( env != null )
            return new Rectangle2D.Double( env.getMinX(), env.getMinY(), env.getWidth(), env.getHeight() );
        else
            return null;
    }

    @Override
    public void close() {
        m_BaseDataObject = null;
    }

    @Override
    public String getFilename() {
        return "no file";
    }

    @Override
    public String getName() {
        return m_Name;
    }

    @Override
    public void open() {
        m_BaseDataObject = m_Features;
    }

    @Override
    public void postProcess()
                            throws Exception {
        // e.g. save file after process
    }

    @Override
    public void setName( String sName ) {
        m_Name = sName;
    }

    /**
     * Sets the shape type on the basis of the {@link Geometry}.
     * 
     * @param geom
     *            {@link Geometry}.
     */
    private void setShapeType( Geometry geom ) {

        // LOGGING: type of this geometry
        // LOG.info( "SHAPE TYPE (geometry): " + geom.getGeometryType() );

        try {

            // GeometryType
            Class<?> geomType = Class.forName( "com.vividsolutions.jts.geom." + geom.getGeometryType() );

            // Polygon
            if ( geomType.equals( Polygon.class ) )
                setNewShapeType( IVectorLayer.SHAPE_TYPE_POLYGON );

            else

            // LineString
            if ( geomType.equals( LineString.class ) )
                setNewShapeType( IVectorLayer.SHAPE_TYPE_LINE );

            else

            // Point
            if ( geomType.equals( Point.class ) )
                setNewShapeType( IVectorLayer.SHAPE_TYPE_POINT );

            else

            // MultiPolygon
            if ( geomType.equals( MultiPolygon.class ) )
                setNewShapeType( IVectorLayer.SHAPE_TYPE_POLYGON );

            else

            // MultiLineString
            if ( geomType.equals( MultiLineString.class ) )
                setNewShapeType( IVectorLayer.SHAPE_TYPE_LINE );

            else

            // MultiPoint
            if ( geomType.equals( MultiPoint.class ) )
                setNewShapeType( IVectorLayer.SHAPE_TYPE_POINT );

            else

            // GeometryCollection
            if ( geomType.equals( GeometryCollection.class ) )
                setNewShapeType( IVectorLayer.SHAPE_TYPE_MIXED );

            else {

                // Unknown shape type
                setNewShapeType( IVectorLayer.SHAPE_TYPE_WRONG );
                LOG.error( "Unknown shape type: " + geom.getGeometryType() );

            }

        } catch ( ClassNotFoundException e ) {
            e.printStackTrace();
        }

    }

    /**
     * Sets the shape type on the basis of the {@link IVectorLayer}.SHAPE_TYPES.
     * 
     * @param shapeType
     *            Shape Type of {@link IVectorLayer}.
     */
    private void setNewShapeType( int shapeType ) {

        // update shape type
        if ( m_ShapeType == -2 ) { // first geometry
            m_ShapeType = shapeType;
        } else {// following geometries
            if ( m_ShapeType != IVectorLayer.SHAPE_TYPE_WRONG ) {
                if ( m_ShapeType != IVectorLayer.SHAPE_TYPE_MIXED ) {
                    if ( m_ShapeType != shapeType ) {
                        if ( shapeType == IVectorLayer.SHAPE_TYPE_WRONG )
                            m_ShapeType = IVectorLayer.SHAPE_TYPE_WRONG;
                        else
                            m_ShapeType = IVectorLayer.SHAPE_TYPE_MIXED;
                    }
                } else {
                    if ( shapeType == IVectorLayer.SHAPE_TYPE_WRONG )
                        m_ShapeType = IVectorLayer.SHAPE_TYPE_WRONG;
                }
            }
        }
    }

}
